package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.SeaCreatureJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.fishing.SeaCreatureFishEvent
import at.hannibal2.skyhanni.features.fishing.seaCreatureXMLGui.SpecificSeaCreatures
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ColorUtils.toRGB
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.chat.TextHelper
import at.hannibal2.skyhanni.utils.compat.appendWithColor
import at.hannibal2.skyhanni.utils.compat.componentBuilder
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.MutableComponent
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object SeaCreatureManager {
    private var lastDoubleHookTime = SimpleTimeMark.farPast()

    private val seaCreatureMap = mutableMapOf<String, SeaCreature>()
    var allFishingMobs = mapOf<String, SeaCreature>()
    var allVariants = mapOf<String, List<String>>()

    private val patternGroup = RepoPattern.group("fishing.seacreature")

    /**
     * REGEX-TEST: It's a Double Hook! Woot woot!
     * REGEX-TEST: It's a Double Hook!
     */
    private val doubleHookPattern by patternGroup.pattern(
        "doublehook.colorless",
        "It's a Double Hook!(?: Woot woot!)?",
    )

    private val config get() = SkyHanniMod.feature.fishing

    // Does not reset lastDoubleHookTime at the end due to intercepting messages
    @HandleEvent(onlyOnSkyblock = true)
    private fun onChat(event: SkyHanniChatEvent.Allow) {
        val message = event.cleanMessage
        if (doubleHookPattern.matches(message)) {
            if (config.compactDoubleHook) {
                event.blockedReason = "double_hook"
            }
            lastDoubleHookTime = SimpleTimeMark.now()
            return
        }
        getSeaCreatureFromMessage(message)?.let {
            val isDoubleHook = wasDoubleHookRecently()
            SeaCreatureFishEvent(it, isDoubleHook).post()

            if (config.seaCreatureTracker.hideChat) {
                event.blockedReason = "sea_creature_tracker"
                lastDoubleHookTime = SimpleTimeMark.farPast()
            }
            return
        }
    }

    // if you can do it better make a pr
    @HandleEvent(onlyOnSkyblock = true)
    private fun onChat(event: SkyHanniChatEvent.Modify) {
        val message = event.cleanMessage
        if (doubleHookPattern.matches(message)) {
            lastDoubleHookTime = SimpleTimeMark.now()
            return
        }

        // Does not reset lastDoubleHookTime on fail due to intercepting messages like:
        // Auto pet rule, thunder bottle, reindrake empty line + global message
        val seaCreature = getSeaCreatureFromMessage(message) ?: return

        val isDoubleHook = wasDoubleHookRecently()

        val edited = componentBuilder {
            val doubleHookComponent = TextHelper.text("DOUBLE HOOK!").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)

            if (isDoubleHook && config.compactDoubleHook && config.compactDoubleHookPosition == CompactDoubleHookPosition.LEFT) {
                append(doubleHookComponent)
                appendWithColor(" ", ChatFormatting.RESET)
            }
            if (config.shortenFishingMessage) {
                val name = seaCreature.componentDisplayName
                val aOrAn = StringUtils.optionalAn(seaCreature.name)
                appendWithColor("You caught $aOrAn", ChatFormatting.BLUE)
                append(name)
                appendWithColor("!", ChatFormatting.BLUE)
            }
            if (config.compactDoubleHook && isDoubleHook) {
                if (config.compactDoubleHookPosition == CompactDoubleHookPosition.RIGHT) {
                    append(doubleHookComponent)
                }
            }
        } as MutableComponent

        lastDoubleHookTime = SimpleTimeMark.farPast()
        if (!anyMessageEditorEnabled()) return
        event.replaceComponent(edited, "sea_creature")
    }

    @HandleEvent
    private fun onRepoReload(event: RepositoryReloadEvent) {
        seaCreatureMap.clear()
        allFishingMobs = emptyMap()

        val data = event.getConstant<Map<String, SeaCreatureJson>>("SeaCreatures")
        val allFishingMobs = mutableMapOf<String, SeaCreature>()

        val variants = mutableMapOf<String, List<String>>()

        for ((variantName, variant) in data) {
            val chatColor = variant.chatColor
            val variantFishes = mutableListOf<String>()
            variants[variantName] = variantFishes
            for ((name, seaCreature) in variant.seaCreatures) {
                val chatMessage = seaCreature.chatMessage
                val fishingExperience = seaCreature.fishingExperience
                val rarity = seaCreature.rarity
                val rare = seaCreature.rare
                val lootshareSphere = seaCreature.lootshareSphereOverride
                val oldNames = seaCreature.oldNames.orEmpty()
                val componentColor = seaCreature.rgbColor ?: ChatFormatting.getByCode(chatColor[1])?.toRGB() ?: ErrorManager.skyHanniError(
                    "Failed to convert from legacy code to ChatFormatting", "color" to chatColor[1],
                )

                val creature = SeaCreature(name, fishingExperience, chatColor, rare, rarity, lootshareSphere, oldNames, componentColor)
                seaCreatureMap[chatMessage] = creature
                for (alternateMessage in seaCreature.alternateMessages.orEmpty()) {
                    seaCreatureMap[alternateMessage] = creature
                }
                allFishingMobs[name] = creature
                variantFishes.add(name)
            }
        }
        SeaCreatureManager.allFishingMobs = allFishingMobs
        allVariants = variants
        SpecificSeaCreatures.saveSeaCreatures(SpecificSeaCreatures.updateList())
    }

    private fun wasDoubleHookRecently(): Boolean =
        lastDoubleHookTime.passedSince() <= 100.milliseconds

    private fun getSeaCreatureFromMessage(message: String): SeaCreature? {
        return seaCreatureMap.getOrDefault(message, null)
    }

    enum class CompactDoubleHookPosition(private val displayName: String) {
        LEFT("Left"),
        RIGHT("Right"),
        ;

        override fun toString() = displayName
    }

    private fun anyMessageEditorEnabled(): Boolean = config.compactDoubleHook || config.shortenFishingMessage

}
