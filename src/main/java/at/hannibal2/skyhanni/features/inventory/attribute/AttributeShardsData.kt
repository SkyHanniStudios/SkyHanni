package at.hannibal2.skyhanni.features.inventory.attribute

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.enoughupdates.ItemResolutionQuery
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.config.features.inventory.AttributeShardsConfig
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.neu.NeuAttributeShardData
import at.hannibal2.skyhanni.data.jsonobjects.repo.neu.NeuAttributeShardJson
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.item.ShardEvent
import at.hannibal2.skyhanni.events.item.ShardGainEvent
import at.hannibal2.skyhanni.events.item.ShardSource
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryDetector
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getCleanLore
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessary
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SafeItemStack
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.UtilsPatterns
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.jetbrains.annotations.VisibleForTesting
import java.util.regex.Pattern
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
@Suppress("LargeClass")
object AttributeShardsData {
    val config get(): AttributeShardsConfig = SkyHanniMod.feature.inventory.attributeShards
    private val storage get() = ProfileStorageData.profileSpecific?.attributeShards

    private var attributeLevelling = mapOf<LorenzRarity, List<Int>>()
    var unconsumableAttributes = listOf<String>()
    private var attributeInfo = mapOf<String, NeuAttributeShardData>()
    private var internalNameToShard = mapOf<NeuInternalName, String>()
    private var attributeAbilityNameToShard = mapOf<String, String>()
    private var shardIdToShard = mapOf<String, String>()
    private var shardNameToShard = mapOf<String, String>()

    private val validItemSlots = 9..44
    private const val ADVANCED_MODE_SLOT = 52
    private const val BAZAAR_PREFIX = "SHARD_"

    var maxShards = 0
        private set

    val attributeMenuInventory = InventoryDetector { attributeMenuPattern }
    val huntingBoxInventory = InventoryDetector { huntingBoxPattern }
    val bazaarShardsInventory = InventoryDetector(
        onOpenInventory = { DelayedRun.runNextTick { AttributeShardOverlay.updateDisplay() } },
    ) { bazaarShardsInventoryPattern }
    val confirmFusionInventory = InventoryDetector(
        onOpenInventory = { DelayedRun.runNextTick { FusionData.updateFusionData() } },
    ) { confirmFusionPattern }
    val fusionBoxInventory = InventoryDetector { fusionBoxPattern }
    val shardFusionInventory = InventoryDetector { shardFusionPattern }

    private var lastSyphonedMessage = SimpleTimeMark.farPast()

    private val huntingBoxSeenShards = mutableSetOf<String>()
    private val huntingBoxSeenPages = mutableSetOf<Int>()

    private val patternGroup = RepoPattern.group("inventory.attributeshards")

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: (1/3) Oddities ➜ Shards
     * REGEX-TEST: Oddities ➜ Shards
     */
    val bazaarShardsInventoryPattern by patternGroup.pattern(
        "bazaar.shards.inventory",
        """(?:\(\d+/\d+\) )?Oddities ➜ Shards""",
    )

    /**
     * REGEX-TEST: Attribute Menu
     * REGEX-TEST: (1/3) Attribute Menu
     * REGEX-TEST: (11/13) Attribute Menu
     */
    private val attributeMenuPattern by patternGroup.pattern(
        "attribute-menu",
        """(?:\(\d+/\d+\) )?Attribute Menu""",
    )

    /**
     * REGEX-TEST: Hunting Box
     * REGEX-TEST: (1/3) Hunting Box
     * REGEX-TEST: (10/13) Hunting Box
     */
    private val huntingBoxPattern by patternGroup.pattern(
        "hunting-box",
        """(?:\(\d+/\d+\) )?Hunting Box""",
    )

    /**
     * REGEX-TEST: Hunting Box
     * REGEX-TEST: (1/3) Hunting Box
     * REGEX-TEST: (10/13) Hunting Box
     */
    private val huntingBoxPagePattern by patternGroup.pattern(
        "hunting-box.page",
        """(?:\((?<page>\d+)/(?<pages>\d+)\) )?Hunting Box""",
    )

    /**
     * REGEX-TEST: Query: hey
     * REGEX-TEST: Query: Voracious Spider
     */
    private val huntingBoxSearchQueryPattern by patternGroup.pattern(
        "hunting-box.search-query.colorless",
        "Query: .+",
    )

    /**
     * REGEX-TEST: Confirm Fusion
     */
    private val confirmFusionPattern by patternGroup.pattern(
        "confirm-fusion",
        "Confirm Fusion",
    )

    /**
     * REGEX-TEST: Fusion Box
     * REGEX-TEST: (1/3) Fusion Box
     * REGEX-TEST: (10/13) Fusion Box
     */
    private val fusionBoxPattern by patternGroup.pattern(
        "fusion-box",
        """(?:\(\d+/\d+\) )?Fusion Box""",
    )

    private val shardFusionPattern by patternGroup.pattern(
        "shard-fusion",
        "Shard Fusion",
    )

    /**
     * REGEX-TEST: Nature Elemental
     * REGEX-TEST: Berry Eater 9
     * REGEX-TEST: Berry Eater IX
     * REGEX-TEST: Essence of Ice I
     * REGEX-TEST: Advanced Mode
     */
    val attributeShardNamePattern by patternGroup.pattern(
        "name.with-tier",
        """(?<name>.+?)(?: (?<tier>[IVXL]+|[0-9]+))?$""",
    )

    /**
     * REGEX-TEST: Enabled: Yes
     * REGEX-TEST: Enabled: No
     */
    private val attributeStatePattern by patternGroup.pattern(
        "state.enabled",
        "Enabled: (?:(?<enabled>Yes)|No)",
    )

    /**
     * REGEX-TEST: Syphon 1 shard to unlock!
     * REGEX-TEST: Syphon 1 shard to level up!
     * REGEX-TEST: Syphon 3 shards to level up!
     * REGEX-TEST: Syphon 3 more to level up!
     * REGEX-TEST: Syphon 1,000 shards to level up!
     */
    private val syphonAmountPattern by patternGroup.pattern(
        "syphon.amount.remaining-formatted",
        """Syphon (?<amount>[\d,]+) (?:shards?|more) to (?:level up|unlock)!""",
    )

    /**
     * REGEX-TEST: Veil (Combat)
     * REGEX-TEST: Yummy X (Foraging)
     * REGEX-TEST: Diurnal Animal 5 (Foraging)
     */
    private val attributeShardNameLorePattern by patternGroup.pattern(
        "name.lore.with-tier",
        """(?<name>.+?)(?: (?<tier>[IVXL]+|[0-9]+))? \(\w+\)$""",
    )

    /**
     * REGEX-TEST: Attribute Maxed!
     */
    private val attributeMaxedPattern by patternGroup.pattern(
        "hunting-box.attribute-maxed",
        "Attribute Maxed!",
    )

    /**
     * REGEX-TEST: Owned: 1 Shard
     * REGEX-TEST: Owned: 3 Shards
     * REGEX-TEST: Owned: 71 Shards
     * REGEX-TEST: Owned: 1,729 Shards
     */
    val amountOwnedPattern by patternGroup.pattern(
        "owned.colorless",
        """Owned: (?<amount>[\d,]+) Shards?""",
    )

    /**
     * REGEX-TEST: Required to fuse: 5
     */
    val requiredToFusePattern by patternGroup.pattern(
        "fuse.required.colorless",
        """Required to fuse: (?<amount>\d+)""",
    )

    /**
     * REGEX-TEST: +1 Arthropod Ruler Attribute (Level 1) - 2 more to upgrade!
     * REGEX-TEST: +1 Arthropod Ruler Attribute (Level 2) - 3 more to upgrade!
     * REGEX-TEST: +2 Essence of Ice Attribute (Level 2) - 1 more to upgrade!
     * REGEX-TEST: +6 Ender Ruler Attribute (Level 3) - 3 more to upgrade!
     * REGEX-FAIL: +43 Essence of Ice Attribute (Level 10) MAXED
     */
    private val shardSyphonedPattern by patternGroup.pattern(
        "chat.syphoned.colorless-formatted",
        """\+(?<amount>[\d,]+) (?<attributeName>.+) Attribute \(Level (?<level>[\d,]+)\) - (?<untilNext>[\d,]+) more to upgrade!""",
    )

    /**
     * REGEX-TEST: +43 Essence of Ice Attribute (Level 10) MAXED
     * REGEX-FAIL: +2 Essence of Ice Attribute (Level 2) - 1 more to upgrade!
     */
    private val shardSyphonedMaxedPattern by patternGroup.pattern(
        "chat.syphoned.maxed.colorless-formatted",
        """\+(?<amount>[\d,]+) (?<attributeName>.+) Attribute \(Level (?<level>[\d,]+)\) MAXED""",
    )

    /**
     * REGEX-TEST: and 7 more...
     */
    private val andMoreMessagePattern by patternGroup.pattern(
        "chat.and.more.colorless-formatted",
        """and (?<amount>[\d,]+) more\.\.\.""",
    )

    private val advancedModeNotUnlockedPattern by patternGroup.pattern(
        "advanced.mode.colorless",
        "Advanced Mode unlocked at 30",
    )

    /**
     * REGEX-TEST: Nature Elemental is now enabled!
     */
    private val attributeEnabledPattern by patternGroup.pattern(
        "chat.enabled.colorless",
        "(?<attributeName>.+) is now enabled!",
    )

    /**
     * REGEX-TEST: Nature Elemental is now disabled!
     */
    private val attributeDisabledPattern by patternGroup.pattern(
        "chat.disabled.colorless",
        "(?<attributeName>.+) is now disabled!",
    )

    /**
     * REGEX-TEST: You caught x2 Bal Shards!
     * REGEX-TEST: You caught a Birries Shard!
     * REGEX-TEST: You caught an Invisibug Shard!
     * REGEX-TEST: You caught an Invisibug Shard!
     * REGEX-TEST: You caught x2 Hideonleaf Shards!
     * REGEX-TEST: You caught x2 Voracious Spider Shards!
     */
    private val caughtShardsPattern by patternGroup.pattern(
        "caught.shards.colorless-formatted",
        """You caught(?: [an]+)?(?: x(?<amount>[\d,]+))? (?<shardName>.+) Shards?!""",
    )

    /**
     * REGEX-TEST:  GOOD CATCH! You caught Water Snake Shard x3!
     * REGEX-TEST:  GREAT CATCH! You caught Giant Water Bug Shard x3!
     * REGEX-TEST:  GOOD CATCH! You caught a Water Snake Shard!
     */
    private val caughtMultipleShardsPattern by patternGroup.pattern(
        "caught-multiple.shards-formatted",
        """\uE025 .+ CATCH! You caught(?: [an]+)? (?<shardName>.+) Shard(?: x(?<amount>[\d,]+))?!"""
    )

    /**
     * REGEX-TEST: LOOT SHARE You received a Glacite Walker Shard for assisting Mealoan!
     * REGEX-TEST: LOOT SHARE You received 2 Mossybit Shards for assisting FallenYeti!
     * REGEX-TEST: LOOT SHARE! You received 2x Parakeet Shard from meowgirlemily catching a Parakeet!
     * REGEX-TEST: LOOT SHARE! You received an Areita Shard from VirulentNyx catching an Areita!
     */
    private val lootShareShardPattern by patternGroup.pattern(
        "loot.share.shard.colorless-formatted",
        """LOOT SHARE!? You received (?:an?|(?<amount>[\d,]+)x?) (?<shardName>.+) Shards? (?:for assisting .*|from .*)!""",
    )

    /**
     * REGEX-TEST: FUSION! You obtained Bolt Shard x2!
     * REGEX-TEST: FUSION! You obtained Bolt Shard x2! NEW!
     * REGEX-TEST: FUSION! You obtained a Tadgang Shard!
     * REGEX-TEST: FUSION! You obtained a Tadgang Shard! NEW!
     */
    private val fusionShardPattern by patternGroup.pattern(
        "fusion.shard.colorless-formatted",
        """FUSION! You obtained(?: an?)? (?<shardName>.+) Shard(?: x(?<amount>[\d,]+))?!(?: NEW!)?""",
    )

    /**
     * REGEX-TEST: CHARM! You charmed the Haggard and received 3 Haggard Shards!
     * REGEX-TEST: CHARM! You charmed the Slug and received 1 Keeled Slug Shard!
     */
    private val charmedShardPattern by patternGroup.pattern(
        "charmed.shard.colorless-formatted",
        """CHARM! You charmed the .+ and received (?<amount>[\d,]+) (?<shardName>.+) Shards?!""",
    )

    /**
     * REGEX-TEST: You sent an Invisibug Shard to your Hunting Box.
     * REGEX-TEST: You sent 6 Voracious Spider Shards to your Hunting Box.
     * REGEX-TEST: You sent a Voracious Spider Shard to your Hunting Box.
     * REGEX-TEST: You sent a Verdant Shard to your Hunting Box.
     */
    private val sentToHuntingBoxPattern by patternGroup.pattern(
        "sent.to.hunting.box.colorless-formatted",
        """You sent (?:an?|(?<amount>[\d,]+)) (?<shardName>.+) Shards? to your Hunting Box.""",
    )

    /**
     * REGEX-TEST: CAPTURE! You caught a Strongarm and gained a Strongarm Shard!
     * REGEX-TEST: CAPTURE! You caught a Gimmiegold and gained a Gimmiegold Shard!
     * REGEX-TEST: CAPTURE! You caught an Areita and gained an Areita Shard!
     * REGEX-TEST: CAPTURE! You caught a Solsnatcher and gained 2x Solsnatcher Shard!
     * REGEX-TEST: CAPTURE! You found Hideyho, and as a reward he gave you a Hideyho Shard!
     * REGEX-TEST: CAPTURE! You found Hideyho, and as a reward he gave you 4x Hideyho Shard!
     * REGEX-TEST: CAPTURE! You found the Hideyho, and as a reward it gave you 4x Hideyho Shard!
     */
    @Suppress("MaxLineLength")
    private val capturedShardPattern by patternGroup.pattern(
        "captured.shard-formatted",
        """CAPTURE! You (?:caught an?|found) .+ and (?:gained|as a reward (?:he|she|they|it) gave you) (?:an?|(?<amount>[\d,]+)x) (?<shardName>.+) Shard!""",
    )

    /**
     * REGEX-TEST: FLOOR DROP! You found Litterbug Shard on the ground!
     * REGEX-TEST: FLOOR DROP! You found Solsnatcher Shard on the ground!
     */
    private val floorDropShardPattern by patternGroup.pattern(
        "floor-drop.shard",
        "FLOOR DROP! You found (?<shardName>.+) Shard on the ground!",
    )

    /**
     * REGEX-TEST: You have been given a Gimmiegold!
     */
    private val givenShardsPattern by patternGroup.pattern(
        "given.shards",
        "You have been given a (?<shardName>.+)!",
    )

    /**
     * REGEX-TEST: SHARD! Your contribution earned you the End Stone Protector Shard!
     */
    private val bossShardPattern by patternGroup.pattern(
        "boss.shard",
        "SHARD! Your contribution earned you the (?<shardName>.+) Shard!",
    )
    // </editor-fold>

    private val shardGainChatPatterns = mapOf<Pattern, ShardSource>(
        caughtShardsPattern to HUNT,
        caughtMultipleShardsPattern to FISHING,
        lootShareShardPattern to HUNT,
        charmedShardPattern to CHARM,
        sentToHuntingBoxPattern to SENT_TO_HUNTING_BOX,
        capturedShardPattern to CAPTURED,
        floorDropShardPattern to FLOOR_DROP,
        givenShardsPattern to GIVEN,
        bossShardPattern to BOSS,
    )

    @HandleEvent(priority = HandleEvent.LOWEST)
    private fun onNeuRepoReload(event: NeuRepositoryReloadEvent) {
        loadShardData(event.getConstant<NeuAttributeShardJson>("attribute_shards"))
    }

    internal fun loadShardData(attributesJson: NeuAttributeShardJson) {
        attributeLevelling = attributesJson.attributeLevelling
        unconsumableAttributes = attributesJson.unconsumableAttributes
        val attributes = attributesJson.attributes
        attributeInfo = attributes.associateBy { it.shardKey() }
        maxShards = attributeInfo.size - unconsumableAttributes.size
        internalNameToShard = attributeInfo.map { (name, info) ->
            info.internalName to name
        }.toMap()
        attributeAbilityNameToShard = attributeInfo.map { (name, info) ->
            info.abilityName to name
        }.toMap()
        shardIdToShard = attributeInfo.map { (name, info) ->
            info.shardId to name
        }.toMap()
        shardNameToShard = buildMap {
            for ((name, info) in attributeInfo) put(normalizeShardName(info.displayName), name)
            for ((name, info) in attributeInfo) {
                val bazaarName = info.bazaarName.asString()
                if (bazaarName.startsWith(BAZAAR_PREFIX)) putIfAbsent(bazaarName.removePrefix(BAZAAR_PREFIX), name)
            }
        }
    }

    private fun normalizeShardName(name: String): String = name.uppercase().replace(" ", "_")

    /** Shards without a Bazaar name (e.g. Rainbug) use their display name for storage. */
    private fun NeuAttributeShardData.shardKey(): String =
        shardStorageKey(bazaarName.asString(), displayName)

    internal fun shardStorageKey(bazaarName: String, displayName: String): String =
        bazaarName.ifEmpty { BAZAAR_PREFIX + normalizeShardName(displayName) }

    /** Accepts both display names and old Bazaar names. */
    fun findInternalNameByShardName(name: String): NeuInternalName? =
        shardNameToShard[normalizeShardName(name)]?.let { attributeInfo[it]?.internalName }

    @HandleEvent(onlyOnSkyblock = true)
    private fun onChat(event: SkyHanniChatEvent.Allow) {
        val message = event.cleanMessage
        shardSyphonedPattern.matchMatcher(message) {
            val attributeName = group("attributeName")
            val level = group("level").formatInt()
            val untilNext = group("untilNext").formatInt()
            val shardName = abilityNameToShardName(attributeName) ?: return
            val shardInternalName = shardNameToInternalName(shardName) ?: return
            processShard(shardInternalName, level, untilNext)

            ShardEvent(shardInternalName, -group("amount").formatInt(), ShardSource.SYPHON).post()

            lastSyphonedMessage = SimpleTimeMark.now()
            return
        }

        shardSyphonedMaxedPattern.matchMatcher(message) {
            val attributeName = group("attributeName")
            val shardName = abilityNameToShardName(attributeName) ?: return
            val shardInternalName = shardNameToInternalName(shardName) ?: return
            processShard(shardInternalName, 10, 0)

            ShardEvent(shardInternalName, -group("amount").formatInt(), ShardSource.SYPHON).post()

            lastSyphonedMessage = SimpleTimeMark.now()
            return
        }

        andMoreMessagePattern.matchMatcher(message) {
            if (lastSyphonedMessage.passedSince() > 1.seconds) return
            if (!config.enabled) return
            val amount = group("amount").formatInt()
            DelayedRun.runNextTick {
                ChatUtils.clickableChat(
                    "§aClick here and scroll through to refresh SkyHanni's attribute overlay data with $amount shards",
                    { HypixelCommands.attributeMenu() },
                )
            }
        }

        attributeEnabledPattern.matchMatcher(message) {
            val attributeName = group("attributeName")
            val shardName = abilityNameToShardName(attributeName) ?: return
            val shardInternalName = shardNameToInternalName(shardName) ?: return
            setAttributeState(shardInternalName, true)
        }

        attributeDisabledPattern.matchMatcher(message) {
            val attributeName = group("attributeName")
            val shardName = abilityNameToShardName(attributeName) ?: return
            val shardInternalName = shardNameToInternalName(shardName) ?: return
            setAttributeState(shardInternalName, false)
        }

        for ((pattern, source) in shardGainChatPatterns) {
            pattern.matchMatcher(message) {
                val shardName = group("shardName")
                val amount = groupOrNull("amount")?.formatInt() ?: 1

                val shardInternalName = ItemResolutionQuery.attributeNameToInternalName(shardName)?.toInternalName()
                if (shardInternalName == null) {
                    ItemUtils.addMissingRepoItem(shardName, "Could not find internal name for attribute shard: $shardName")
                    return
                }

                if (source == SENT_TO_HUNTING_BOX) {
                    ShardEvent(shardInternalName, amount, source).post()
                } else {
                    ShardGainEvent(shardInternalName, amount, source).post()
                }
                return
            }
        }

        fusionShardPattern.matchMatcher(message) {
            val currentFusionData = FusionData.currentFusionData ?: return
            val amount = groupOrNull("amount")?.formatInt() ?: 1
            ShardEvent(currentFusionData.outputShard, amount, ShardSource.FUSE).post()
            ShardEvent(currentFusionData.firstShard.internalName, -currentFusionData.firstShard.amount, ShardSource.FUSE).post()
            ShardEvent(currentFusionData.secondShard.internalName, -currentFusionData.secondShard.amount, ShardSource.FUSE).post()
        }
    }

    @HandleEvent
    private fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Active Attribute Levels")
        event.addIrrelevant {
            for (shardName in attributeInfo.keys) {
                add("- $shardName: Level ${getActiveLevel(shardName)}/10")
            }
        }

        event.title("Attribute Shards Without Data")
        val missing = getMissingDataShards()
        if (missing.isEmpty()) {
            event.addIrrelevant("All attribute shards have data.")
        } else {
            event.addData {
                for (shardName in missing) add("- $shardName")
            }
        }
    }

    private data class MenuShard(val key: String, val tier: Int)

    /** Prefer the lore ID because shard names can differ between the menu and the Bazaar. */
    private fun readMenuShard(cleanName: String, cleanLore: List<String>): MenuShard? {
        val source = UtilsPatterns.attributeSourcePattern.firstMatcher(cleanLore) { group("source") to group("id") }
        val isShardItem = source != null ||
            syphonAmountPattern.anyMatches(cleanLore) ||
            attributeStatePattern.anyMatches(cleanLore)
        if (!isShardItem) return null

        val (abilityName, tier) = attributeShardNamePattern.matchMatcher(cleanName) {
            group("name") to (groupOrNull("tier")?.romanToDecimalIfNecessary() ?: 0)
        } ?: return null

        val key = source?.let { shardIdToShard[it.second] }
            ?: attributeAbilityNameToShard[abilityName]
            ?: attributeAbilityNameToShard[cleanName]
            ?: source?.let { shardNameToShard[normalizeShardName(it.first)] }
            ?: return null
        return MenuShard(key, tier)
    }

    /**
     * Resolves an item of the attribute menu to the internal name of its shard.
     * Used by [at.hannibal2.skyhanni.api.enoughupdates.ItemResolutionQuery].
     */
    fun resolveMenuShardInternalName(cleanName: String, cleanLore: List<String>): NeuInternalName? {
        val shard = readMenuShard(cleanName, cleanLore) ?: return null
        return attributeInfo[shard.key]?.internalName
    }

    private fun processAttributeMenuItems(items: Map<Int, SafeItemStack>) {
        for (item in items.values) {
            try {
                processAttributeMenuItem(item)
            } catch (e: Exception) {
                ErrorManager.logErrorWithData(
                    e, "Failed to read an attribute shard in the attribute menu",
                    "item" to item.cleanName,
                )
            }
        }

        val advancedModeLore = items[ADVANCED_MODE_SLOT]?.getCleanLore().orEmpty()
        advancedModeNotUnlockedPattern.firstMatcher(advancedModeLore) {
            addAllMissingShards()
        }

        AttributeShardOverlay.updateDisplay()
    }

    private fun processAttributeMenuItem(item: SafeItemStack) {
        val cleanLore = item.getCleanLore()
        val shard = readMenuShard(item.cleanName, cleanLore) ?: return
        if (shard.key in unconsumableAttributes) return

        val toNextTier = syphonAmountPattern.firstMatcher(cleanLore) { group("amount").formatInt() }
        processShard(shard.key, shard.tier, toNextTier)
        readAttributeEnabled(cleanLore)?.let { setAttributeState(shard.key, it) }
    }

    internal fun readAttributeEnabled(cleanLore: List<String>): Boolean? =
        attributeStatePattern.firstMatcher(cleanLore) { groupOrNull("enabled") != null }

    private fun addAllMissingShards() {
        val knownShards = storage?.filterValues { it.syphonKnown != false }?.keys.orEmpty()
        if (knownShards.size > 30) return
        for (shardName in attributeInfo.keys) {
            if (shardName in knownShards) continue
            if (shardName in unconsumableAttributes) continue

            processShard(shardName, 0, 1)
        }
    }

    /** Older profiles used an empty key for the shard without a Bazaar name. */
    internal fun migrateLegacyStorage(element: JsonElement): JsonElement {
        val obj = element as? JsonObject ?: return element
        val legacy = obj.remove("") ?: return obj
        val target = shardStorageKey("", "Rainbug")
        if (!obj.has(target)) {
            obj.add(target, legacy)
        }
        return obj
    }

    @HandleEvent
    @VisibleForTesting
    @Suppress("PrivateEventListener")
    internal fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(150, "#profile.attributeShards", ::migrateLegacyStorage)
    }

    private fun getOrCreateData(attributeName: String): ProfileSpecificStorage.AttributeShardData? =
        storage?.getOrPut(attributeName) { ProfileSpecificStorage.AttributeShardData(syphonKnown = false) }

    /** Copies the entries so the overlay can compare them after the next update. */
    fun getTrackedShards(): Map<String, ProfileSpecificStorage.AttributeShardData> {
        val data = storage ?: return emptyMap()
        return data
            .filterKeys { it in attributeInfo && it !in unconsumableAttributes }
            .mapValues { it.value.copy() }
    }

    /** Display names of shards with missing syphon data. */
    fun getMissingDataShards(): List<String> {
        val data = storage ?: return emptyList()
        return attributeInfo.mapNotNull { (key, info) ->
            if (key in unconsumableAttributes) return@mapNotNull null
            val entry = data[key]
            if (entry != null && entry.syphonKnown != false) null else info.displayName
        }.sorted()
    }

    @HandleEvent(onlyOnSkyblock = true)
    private fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (attributeMenuInventory.isInside()) {
            val menuItems = event.inventoryItems
            DelayedRun.runOrNextTick { processAttributeMenuItems(menuItems) }
            return
        }
        if (!huntingBoxInventory.isInside()) return
        val items = event.inventoryItems
        val shardsInBox = mutableSetOf<String>()
        var searchActive = false

        for ((slot, item) in items) {
            if (!isValidSlotNumber(slot)) {
                if (huntingBoxSearchQueryPattern.anyMatches(item.getCleanLore())) searchActive = true
                continue
            }
            try {
                val internalName = item.getInternalNameOrNull() ?: continue
                val key = internalNameToShard[internalName] ?: continue
                shardsInBox.add(key)
                readShardInHuntingBox(key, item)
            } catch (e: Exception) {
                ErrorManager.logErrorWithData(
                    e, "Failed to read an attribute shard in the hunting box",
                    "item" to item.cleanName,
                )
            }
        }

        syncHuntingBoxAmounts(event.inventoryName, shardsInBox, searchActive)
        HuntingBoxValue.processInventory(items)
    }

    private fun readShardInHuntingBox(key: String, item: SafeItemStack) {
        val lore = item.getCleanLore()
        amountOwnedPattern.firstMatcher(lore) {
            setAmountInBox(key, group("amount").formatInt())
        }
        val info = attributeInfo[key] ?: return
        val tier = readHuntingBoxTier(lore, info.abilityName) ?: return
        val remaining = syphonAmountPattern.firstMatcher(lore) { group("amount").formatInt() }
        processShard(key, tier, remaining)
    }

    internal fun readHuntingBoxTier(lore: List<String>, abilityName: String): Int? {
        if (attributeMaxedPattern.anyMatches(lore)) return 10
        for (line in lore) {
            attributeShardNameLorePattern.matchMatcher(line) {
                if (group("name") == abilityName) return groupOrNull("tier")?.romanToDecimalIfNecessary() ?: 0
            }
        }
        return null
    }

    /**
     * Shards missing from the box are gone, not unknown. Skipped while a search or an unvisited page hides shards.
     */
    private fun syncHuntingBoxAmounts(inventoryName: String, shardsInBox: Set<String>, searchActive: Boolean) {
        if (searchActive) return
        val (page, totalPages) = huntingBoxPagePattern.matchMatcher(inventoryName) {
            (groupOrNull("page")?.toInt() ?: 1) to (groupOrNull("pages")?.toInt() ?: 1)
        } ?: return

        if (page == 1) {
            huntingBoxSeenShards.clear()
            huntingBoxSeenPages.clear()
        }
        huntingBoxSeenShards.addAll(shardsInBox)
        huntingBoxSeenPages.add(page)
        if (huntingBoxSeenPages.size < totalPages) return

        storage?.forEach { (shardName, shardData) ->
            if (shardName !in huntingBoxSeenShards) shardData.amountInBox = 0
        }
    }

    private fun setAmountInBox(attributeName: String, amount: Int) {
        getOrCreateData(attributeName)?.amountInBox = amount
    }

    @HandleEvent(priority = HandleEvent.HIGHEST)
    private fun onShardGain(event: ShardEvent) {
        val attributeName = internalNameToShard[event.shardInternalName] ?: return
        if (attributeName in unconsumableAttributes) return
        val existing = storage?.get(attributeName)?.amountInBox ?: 0
        setAmountInBox(attributeName, (existing + event.amount).coerceAtLeast(0))
    }

    private fun processShard(
        internalName: NeuInternalName,
        currentTier: Int,
        toNextTier: Int,
    ) {
        processShard(shardInternalNameToShardName(internalName), currentTier, toNextTier)
    }

    private fun processShard(
        attributeName: String,
        currentTier: Int,
        toNextTier: Int?,
    ) {
        if (attributeName in unconsumableAttributes) return
        val rarity = attributeInfo[attributeName]?.rarity ?: return
        val tierCosts = attributeLevelling[rarity] ?: return
        val totalAmount = syphonedAtTier(tierCosts, currentTier, toNextTier) ?: return
        getOrCreateData(attributeName)?.apply {
            amountSyphoned = totalAmount
            syphonKnown = true
        }
    }

    private fun setAttributeState(
        internalName: NeuInternalName,
        enabled: Boolean,
    ) {
        setAttributeState(shardInternalNameToShardName(internalName), enabled)
    }

    private fun setAttributeState(
        attributeName: String,
        enabled: Boolean,
    ) {
        // Chameleon can appear in the menu without a level.
        if (attributeName in unconsumableAttributes) return
        getOrCreateData(attributeName)?.enabled = enabled
    }

    fun findTierAndAmountUntilNext(shardName: String, totalAmount: Int): Triple<Int, Int, Int> {
        val rarity = attributeInfo[shardName]?.rarity
            ?: ErrorManager.skyHanniError("Unknown attribute shard rarity for $shardName")
        val tierLevelling = attributeLevelling[rarity]
            ?: ErrorManager.skyHanniError("Unknown attribute rarity: $rarity")

        var tier = 0
        var cumulativeCount = 0
        var amountToNextTier = 0

        for (amount in tierLevelling) {
            cumulativeCount += amount
            if (cumulativeCount > totalAmount && amountToNextTier == 0) {
                amountToNextTier = cumulativeCount - totalAmount
            }
            if (cumulativeCount > totalAmount) continue
            tier++
        }
        val amountToMax = (cumulativeCount - totalAmount).coerceAtLeast(0)
        return Triple(tier, amountToNextTier, amountToMax)
    }

    fun shardInternalNameToShardName(internalName: NeuInternalName): String {
        return internalNameToShard[internalName]
            ?: ErrorManager.skyHanniError("Unknown attribute shard internal name: $internalName")
    }

    private fun abilityNameToShardName(ability: String): String? {
        val shardName = attributeAbilityNameToShard[ability]
        if (shardName == null) {
            ItemUtils.addMissingRepoItem(ability, "Could not find shard name for attribute ability: $ability")
        }
        return shardName
    }

    fun shardNameToInternalName(shardName: String): NeuInternalName? {
        val internalName = attributeInfo[shardName]?.internalName
        if (internalName == null) {
            ItemUtils.addMissingRepoItem(shardName, "Could not find internal name for attribute shard: $shardName")
        }
        return internalName
    }

    fun isValidSlotNumber(slot: Int): Boolean {
        if (slot !in validItemSlots) return false
        val modNine = slot % 9
        return modNine != 0 && modNine != 8
    }

    internal fun isTrackedShard(internalName: NeuInternalName): Boolean {
        val key = internalNameToShard[internalName] ?: return false
        return key !in unconsumableAttributes
    }

    fun isAttributeShard(internalName: NeuInternalName): Boolean =
        internalName.asString().let {
            it.startsWith("ATTRIBUTE_SHARD_") && it.endsWith(";1")
        }

    private fun getLevel(shardName: String): Int =
        getSyphonedAmount(shardName).let {
            findTierAndAmountUntilNext(shardName, it).first
        }

    private fun isEnabled(shardName: String): Boolean =
        storage?.get(shardName)?.enabled ?: false

    fun getActiveLevel(shardName: String): Int =
        if (isEnabled(shardName)) getLevel(shardName) else 0

    fun getActiveLevelByAbilityName(abilityName: String): Int =
        attributeAbilityNameToShard[abilityName]?.let(::getActiveLevel) ?: 0

    fun getSyphonedAmount(shardName: String): Int {
        return storage?.get(shardName)?.amountSyphoned ?: 0
    }

    fun getAmountInHuntingBox(shardName: String): Int {
        return storage?.get(shardName)?.amountInBox ?: 0
    }

    fun getAmountUntilMax(shardName: String): Int {
        return findTierAndAmountUntilNext(shardName, getSyphonedAmount(shardName)).third
    }

    fun isInFusionMachine(): Boolean {
        return fusionBoxInventory.isInside() || shardFusionInventory.isInside() || confirmFusionInventory.isInside()
    }

    fun resetHuntingBoxShards() {
        storage?.forEach { it.value.amountInBox = 0 }
        ChatUtils.clickableChat(
            "Reset hunting box shards data. Open the hunting box or click here to update the saved data.",
            { HypixelCommands.huntingBox() },
            "§eClick here to open the hunting box!",
        )
    }

    @HandleEvent
    private fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.registerBrigadier("shresethuntingbox") {
            description = "Resets stored hunting box shards"
            category = USERS_RESET
            simpleCallback {
                resetHuntingBoxShards()
            }
        }
    }

    internal fun syphonedAtTier(tierCosts: List<Int>, tier: Int, remaining: Int?): Int? {
        if (tier !in 0..tierCosts.size) return null
        val earned = tierCosts.take(tier).sum()
        val nextCost = tierCosts.getOrNull(tier) ?: return earned
        if (remaining == null) return earned
        if (remaining !in 0..nextCost) return null
        return earned + nextCost - remaining
    }
}
