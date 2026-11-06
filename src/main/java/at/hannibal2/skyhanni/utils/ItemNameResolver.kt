package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.api.enoughupdates.EnoughUpdatesManager
import at.hannibal2.skyhanni.api.enoughupdates.ItemResolutionQuery
import at.hannibal2.skyhanni.data.model.SkyblockStat
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NeuItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessary
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object ItemNameResolver {
    private val itemNameCache = mutableMapOf<String, NeuInternalName>() // item name -> internal name
    private val HAY_BALE = "HAY_BALE".toInternalName()
    private val HAY_BLOCK = "HAY_BLOCK".toInternalName()
    private val patternGroup = RepoPattern.group("misc.itemnameresolver")

    @Suppress("ReturnCount", "CyclomaticComplexMethod")
    internal fun getInternalNameOrNull(itemName: String): NeuInternalName? {
        // Without this the fallback below would resolve the name anyway, just to a random candidate.
        if (NeuItems.isAmbiguousDisplayName(itemName)) return null

        val lowercase = itemName.lowercase()
        itemNameCache[lowercase]?.let {
            return it
        }

        getInternalNameOrNullIgnoreCase(lowercase)?.let {
            return itemNameCache.getOrPut(lowercase) { it }
        }

        if (itemName == "§cmissing repo item") {
            return itemNameCache.getOrPut(lowercase) { NeuInternalName.MISSING_ITEM }
        }

        val enchantedBookCandidates = enchantedBookCandidates(itemName)
        enchantedBookCandidates.firstOrNull { it.getItemStackOrNull() != null }?.let {
            return itemNameCache.getOrPut(lowercase) { it }
        }
        if (ItemUtils.readBookType(itemName) != null) {
            reportUnresolvedEnchantedBook(itemName, enchantedBookCandidates)
            return null
        }

        resolvePetWithRarity(itemName)?.let {
            return itemNameCache.getOrPut(lowercase) { it }
        }

        if (itemName.endsWith("gemstone", ignoreCase = true)) {
            val split = lowercase.split(" ")
            if (split.size == 3) {
                val gemstoneQuery = "${
                    resolveGemstoneToStat(split[1])?.hypixelIcon ?: ' '
                } ${split.joinToString("_").allLettersFirstUppercase()}"
                ItemResolutionQuery.findInternalNameByDisplayName(gemstoneQuery, true)?.let {
                    return itemNameCache.getOrPut(lowercase) { it }
                }
            }
        }

        val internalName = when (itemName) {
            "SUPERBOOM TNT" -> "SUPERBOOM_TNT".toInternalName()
            else -> {
                ItemResolutionQuery.findInternalNameByDisplayName(itemName, true)?.let {
                    // This fixes a NEU bug with §9Hay Bale (cosmetic item)
                    if (it == HAY_BALE) HAY_BLOCK else it
                } ?: return null
            }
        }

        itemNameCache[lowercase] = internalName
        return internalName
    }

    fun resolveEnchantedBook(displayName: String): NeuInternalName? {
        val candidates = enchantedBookCandidates(displayName)
        return candidates.firstOrNull { it.getItemStackOrNull() != null } ?: run {
            reportUnresolvedEnchantedBook(displayName, candidates)
            null
        }
    }

    fun enchantedBookCandidates(displayName: String): List<NeuInternalName> {
        val bookName = ItemUtils.readBookType(displayName) ?: displayName
        val formatted = ItemResolutionQuery.resolveEnchantmentByName(bookName)
        val clean = UtilsPatterns.cleanEnchantedNamePattern.matchMatcher(bookName.removeColor().trim()) {
            val name = group("name").trim().replace("'", "").replace(" ", "_").replace("-", "_")
            val rawInternalName = "$name;${group("level").romanToDecimalIfNecessary()}"
            listOf(fixEnchantmentName(rawInternalName), fixEnchantmentName("ULTIMATE_$rawInternalName"))
        }.orEmpty()
        return listOfNotNull(formatted).plus(clean).distinct()
    }

    fun enchantedBookCommandId(internalName: String): String {
        val (name, level) = internalName.split(";", limit = 2)
        return "ENCHANTED_BOOK_${name}_$level"
    }

    private fun reportUnresolvedEnchantedBook(displayName: String, candidates: List<NeuInternalName>) {
        if (EnoughUpdatesManager.inLoadingState() || NeuItems.allInternalNames.isEmpty()) return
        ErrorManager.logErrorStateWithData(
            "Could not resolve an enchanted book",
            "No enchanted book internal name found in the NEU repo",
            "displayName" to displayName,
            "attemptedInternalNames" to candidates.joinToString { it.asString() },
        )
    }

    private fun resolveGemstoneToStat(itemName: String): SkyblockStat? =
        when (itemName) {
            "jade" -> MINING_FORTUNE
            "peridot" -> FARMING_FORTUNE
            "citrine" -> FORAGING_FORTUNE
            "amethyst" -> DEFENSE
            "ruby" -> HEALTH
            "amber" -> MINING_SPEED
            "opal" -> TRUE_DEFENSE
            "topaz" -> PRISTINE
            "onyx" -> CRIT_DAMAGE
            "sapphire" -> INTELLIGENCE
            "aquamarine" -> FISHING_SPEED
            "jasper" -> STRENGTH
            else -> null
        }

    private fun resolvePetWithRarity(itemName: String): NeuInternalName? {
        val splits = itemName.split(" ").takeIf { it.size > 1 } ?: return null
        val rarityLocation = splits.indexOfFirst { LorenzRarity.getByName(it) != null }
        val expectedRarityLocations = listOf(0, splits.size - 1)
        if (rarityLocation !in expectedRarityLocations) return null
        val petName = splits.filterIndexed { index, _ -> index != rarityLocation }.joinToString("_").uppercase()
        val petRarity = LorenzRarity.getByName(splits[rarityLocation]) ?: return null
        val internalName = "$petName;${petRarity.neuPetIndex}".toInternalName()
        return internalName.takeIf { it.getItemStackOrNull() != null }
    }

    /**
     * REGEX-TEST: ULTIMATE_DUPLEX;5
     */
    private val duplexPattern by patternGroup.pattern(
        "enchantment.duplex",
        "ULTIMATE_DUPLEX;(?<tier>\\d+)",
    )

    /**
     * REGEX-TEST: LOOTING_NEW;7
     * REGEX-TEST: LUCK_NEW;7
     */
    private val newEnchantPattern by patternGroup.pattern(
        "enchantment.new",
        "(?<name>\\w+?)_NEW;(?<tier>\\d+)",
    )

    fun fixEnchantmentName(originalName: String): NeuInternalName {
        val cleanName = originalName.removeColor().uppercase().replace("ULTIMATE_ULTIMATE_", "ULTIMATE_")
        val withoutNew = newEnchantPattern.matchMatcher(cleanName) { "${group("name")};${group("tier")}" } ?: cleanName
        val fixed = duplexPattern.matchMatcher(withoutNew) { "ULTIMATE_REITERATE;${group("tier")}" } ?: withoutNew
        return fixed.toInternalName()
    }

    private fun getInternalNameOrNullIgnoreCase(itemName: String): NeuInternalName? {
        // supports colored names, rarities
        NeuItems.allItemsCache[itemName]?.let {
            itemNameCache[itemName] = it
            return it
        }

        // if nothing got found with colors, try without colors
        val removeColor = itemName.removeColor()
        return NeuItems.allItemsCache.filter { it.key.removeColor() == removeColor }.values.firstOrNull()
    }

    internal fun clearCache() {
        itemNameCache.clear()
    }
}
