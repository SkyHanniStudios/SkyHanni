package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.IslandLeaveEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.slayer.SlayerChangeEvent
import at.hannibal2.skyhanni.events.slayer.SlayerProgressChangeEvent
import at.hannibal2.skyhanni.events.slayer.SlayerQuestCompleteEvent
import at.hannibal2.skyhanni.events.slayer.SlayerStateChangeEvent
import at.hannibal2.skyhanni.features.misc.pathfind.AreaNode
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.features.slayer.SlayerType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getNpcPriceOrNull
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPriceName
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessaryOrNull
import at.hannibal2.skyhanni.utils.PlayerUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.collection.TimeLimitedCache
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration.Companion.minutes
import at.hannibal2.skyhanni.features.slayer.SlayerType as Type

@SkyHanniModule
object SlayerApi {

    val config get() = SkyHanniMod.feature.slayer
    private val trackerConfig get() = config.itemProfitTracker

    private val patternGroup = RepoPattern.group("slayer.api")

    private const val GRACE_UPDATE_COUNT = 3

    // <editor-fold desc="Patterns">
    /**
     * WRAPPED-REGEX-TEST: "  SLAYER QUEST STARTED!"
     */
    private val questStartPattern by patternGroup.pattern(
        "quest.start",
        "\\s*SLAYER QUEST STARTED!",
    )

    /**
     * WRAPPED-REGEX-TEST: "  SLAYER QUEST COMPLETE!"
     */
    private val questCompletePattern by patternGroup.pattern(
        "quest.complete",
        "\\s*SLAYER QUEST COMPLETE!",
    )

    /**
     * WRAPPED-REGEX-TEST: "  SLAYER QUEST FAILED!"
     */
    private val questFailedPattern by patternGroup.pattern(
        "quest.failed",
        "\\s*SLAYER QUEST FAILED!",
    )

    /**
     * WRAPPED-REGEX-TEST: "  YOU COCOONED YOUR SLAYER BOSS"
     */
    private val cocoonPattern by patternGroup.pattern(
        "cocooned.colorless",
        "\\s+YOU COCOONED YOUR SLAYER BOSS",
    )

    /**
     * REGEX-TEST: SLAYER BOSS! The Bloodfiend III spawned!
     */
    private val bossStartPattern by patternGroup.pattern(
        "boss.start",
        "SLAYER BOSS! The (?<name>.*) (?<tier>[IVX]+) spawned!"
    )

    /**
     * WRAPPED-REGEX-TEST: "  NICE! SLAYER BOSS SLAIN!"
     */
    private val bossSlainPattern by patternGroup.pattern(
        "boss.slain",
        "\\s*NICE! SLAYER BOSS SLAIN!",
    )

    // TODO: Make the SlayerProgressChangeEvent have a sealed class for the progress types
    /**
     * REGEX-TEST: 38/40 Kills
     * REGEX-TEST: (0/4.8k) Combat XP
     */
    private val grindingSlayerPattern by patternGroup.pattern(
        "grinding.progress",
        ".*Combat.*|.*Kills.*",
    )
    // </editor-fold>

    private val nameCache = TimeLimitedCache<Pair<NeuInternalName, Int>, Pair<String, Double>>(1.minutes)

    var questStartTime = SimpleTimeMark.farPast()

    /**
     * Are we having the right slayer quest in the right area?
     */
    var isInCorrectArea = false

    /**
     * Are we in any area that is good for any slayer? - ignoring if we have an active quest
     */
    var isInAnyArea = false

    // for an enum, use activeType
    var latestCategory = ""
    var tier = 0

    var latestWrongAreaWarningTime = SimpleTimeMark.farPast()

    /**
     * What is the current progress of the slayer boss? could be a string with text, or percentage, or x/x kills.
     */
    var latestProgress = ""

    /**
     * What slayer type should be fought in the current area we are in
     */
    var currentAreaType: SlayerType? = null

    /**
     * How many consecutive updates have we seen that are invalid?
     */
    private var invalidUpdates = 0

    private val outsideRiftData = SlayerData()
    private val insideRiftData = SlayerData()

    /**
     * what are we currently doing with the slayer? grinding,
     */
    val state get() = getCurrentData().currentState

    /**
     * The enum type of slayer currently doing
     */
    val activeType get() = getCurrentData().type

    /**
     * Are we currently fighting a slayer boss?
     */
    fun isInBossFight() = state == ActiveQuestState.BOSS_FIGHT

    private class SlayerData {
        var currentState: ActiveQuestState = ActiveQuestState.NO_ACTIVE_QUEST
        var currentStateRaw: String? = null
        var type: Type? = null
    }

    private fun getCurrentData() = if (RiftApi.inRift()) insideRiftData else outsideRiftData

    /**
     * Do we have a slayer quest in the scoreboard?
     */
    fun hasActiveQuest() = latestCategory != ""

    fun getItemNameAndPrice(internalName: NeuInternalName, amount: Int): Pair<String, Double> =
        nameCache.getOrPut(internalName to amount) {
            val price = internalName.getPrice()
            val npcPrice = internalName.getNpcPriceOrNull() ?: 0.0
            val maxPrice = npcPrice.coerceAtLeast(price)
            val totalPrice = maxPrice * amount

            internalName.getPriceName(amount, pricePer = maxPrice) to totalPrice
        }

    @HandleEvent
    private fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Slayer")

        if (!hasActiveQuest()) {
            event.addIrrelevant("no active slayer quest")
            return
        }

        event.addData {
            add("activeType: $activeType")
            add("isInCorrectArea: $isInCorrectArea")
            if (!isInCorrectArea) {
                add("currentAreaType: $currentAreaType")
                add(" graph area: ${SkyBlockUtils.graphArea}")
                add(" /shtestwaypoint ${PlayerUtils.blockPosition().toLocalFormat()} pathfind")
            }
            add("isInAnyArea: $isInAnyArea")
            add("latestProgress: '$latestProgress'")

            val data = getCurrentData()
            add("active data:")
            add("  type: ${data.type}")
            add("  currentState: ${data.currentState}")
            add("  currentStateRaw: ${data.currentStateRaw}")
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    private fun onChat(event: SkyHanniChatEvent.Allow) {
        val message = event.cleanMessage

        when {
            questStartPattern.matches(message) -> {
                questStartTime = SimpleTimeMark.now()
            }
            questCompletePattern.matches(message) -> {
                SlayerQuestCompleteEvent.post()
            }
            questFailedPattern.matches(message) -> {
                updateState(FAILED)
            }
            cocoonPattern.matches(message) -> {
                updateState(COCOONED)
            }
            bossSlainPattern.matches(message) -> {
                updateState(SLAIN)
            }
            else -> {
                bossStartPattern.matchMatcher(message) {
                    val name = group("name")
                    val data = getCurrentData()
                    data.type = Type.getByName(name)
                    updateState(BOSS_FIGHT)
                }
            }
        }
    }

    private fun updateArea() {
        if (SkyBlockUtils.isStrandedProfile) {
            isInAnyArea = true
            isInCorrectArea = true
        } else {
            isInAnyArea = currentAreaType != null
            isInCorrectArea = currentAreaType == activeType && currentAreaType != null
        }
    }

    private fun getSlayerLines(): Pair<List<String>, SlayerLinesSource> {
        val scoreboardLines = ScoreboardData.sidebarLinesFormatted
            .map { it.removeColor().trim() }
            .dropWhile { it != "Slayer Quest" }
        if (scoreboardLines.isNotEmpty()) return scoreboardLines to SlayerLinesSource.SCOREBOARD

        val tabLines = TabWidget.SLAYER.lines.map { it.string.removeColor().trim() }
        if (tabLines.isNotEmpty()) return tabLines to SlayerLinesSource.TAB

        return emptyList<String>() to SlayerLinesSource.NONE
    }

    // returns null if no slayer quest is found, and exception if the parsing fails
    private fun getParsedSlayerOrNull(lines: List<String>): ParsedSlayer? {
        val questIndex = lines.indexOfFirst { Type.getByName(it) != null }
        if (questIndex == -1) return null

        val category = lines[questIndex]
        val type = Type.getByName(category) ?: return null
        val progress = lines.getOrNull(questIndex + 1) ?: throw SlayerParseException("Progress line missing for category '$category'")

        val tierString = category.substringAfterLast(' ', "")
        val parsedTier =
            tierString.romanToDecimalIfNecessaryOrNull() ?: throw SlayerParseException("Failed to parse tier from category '$category'")

        return ParsedSlayer(
            type = type,
            category = category,
            tier = parsedTier,
            progress = progress,
        )
    }

    private fun updateSlayerState() {
        if (ProfileStorageData.profileSpecific == null) return

        val (lines, source) = getSlayerLines()
        val parsed = try {
            getParsedSlayerOrNull(lines)
        } catch (e: SlayerParseException) {
            invalidUpdates++
            if (invalidUpdates == GRACE_UPDATE_COUNT) {
                val message = "Slayer Exception: ${e.message}"
                ErrorManager.skyHanniError(
                    message,
                    "lines" to lines,
                    "source" to source,
                )
            }
            return
        }
        invalidUpdates = 0

        val progress = parsed?.progress ?: "no slayer"

        updateCategory(parsed)
        updateProgress(progress)
        updateActiveState(parsed, progress)
        updateArea()
    }

    private fun updateCategory(parsed: ParsedSlayer?) {
        val category = parsed?.category.orEmpty()
        if (category == latestCategory) return

        val old = latestCategory
        latestCategory = category
        tier = parsed?.tier ?: 0

        ChatUtils.debug("SlayerApi: $old -> $category")
        SlayerChangeEvent(old, category).post()
    }

    private fun updateProgress(progress: String) {
        if (progress == latestProgress) return

        ChatUtils.debug("SlayerApi: $latestProgress -> $progress")
        SlayerProgressChangeEvent(latestProgress, progress).post()
        latestProgress = progress
    }

    private fun updateActiveState(parsed: ParsedSlayer?, progress: String) {
        val data = getCurrentData()
        data.type = parsed?.type

        if (parsed == null) {
            if (state != COCOONED) updateState(NO_ACTIVE_QUEST)
        } else if (grindingSlayerPattern.matches(progress)) {
            updateState(GRINDING)
        }
    }

    private fun updateState(newState: ActiveQuestState) {
        val data = getCurrentData()
        val oldState = data.currentState
        if (oldState == newState) return
        ChatUtils.debug("SlayerApi: ${data.currentState} -> $newState")
        data.currentState = newState
        data.currentStateRaw = newState.rawName
        SlayerStateChangeEvent(newState).post()
    }

    @HandleEvent(ScoreboardUpdateEvent::class, onlyOnSkyblock = true)
    private fun onScoreboardChange() {
        updateSlayerState()
    }

    @HandleEvent(onlyOnSkyblock = true)
    private fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.SLAYER)) return
        updateSlayerState()
    }

    enum class ActiveQuestState {
        GRINDING, // spawning, collecting combat xp
        BOSS_FIGHT,
        FAILED,
        SLAIN,
        COCOONED,
        NO_ACTIVE_QUEST,
        ;

        internal val rawName: String? get() {
            return if (this == NO_ACTIVE_QUEST)  null
            else name.lowercase().replace('_', ' ')
        }
    }

    @HandleEvent(priority = HandleEvent.LOW)
    private fun onAreaChange() {
        currentAreaType = checkTypeForCurrentArea()
        updateArea()
    }

    @HandleEvent
    private fun onConfigLoad() {
        with(trackerConfig) {
            ConditionalUtils.onToggle(revenantInGraveyard, voidgloomInNest, voidgloomInNoArea) {
                currentAreaType = checkTypeForCurrentArea()
                updateArea()
            }
        }
    }

    @HandleEvent
    private fun onIslandLeave(event: IslandLeaveEvent) {
        val data = if (event.island == THE_RIFT) insideRiftData else outsideRiftData
        when (data.currentState) {
            BOSS_FIGHT,
            COCOONED -> {
                updateState(GRINDING)
            }
            else -> {}
        }
    }

    // TODO USE SH-REPO
    private fun checkTypeForCurrentArea() = when (SkyBlockUtils.graphArea) {
        "Graveyard" -> if (trackerConfig.revenantInGraveyard.get() && IslandType.HUB.isInIsland()) Type.REVENANT else null
        "Revenant Cave", "Crypts" -> Type.REVENANT

        "Spider Mound",
        "Arachne's Burrow",
        "Arachne's Sanctuary",
        "Burning Desert",
        -> Type.TARANTULA

        "Ruins",
        "Howling Cave",
        "Soul Cave",
        "Spirit Cave",
        -> Type.SVEN

        "Void Sepulture",
        "Zealot Bruiser Hideout",
        -> Type.VOID

        "Dragon's Nest" -> if (trackerConfig.voidgloomInNest.get() && IslandType.THE_END.isInIsland()) Type.VOID else null
        AreaNode.NO_AREA -> if (trackerConfig.voidgloomInNoArea.get() && IslandType.THE_END.isInIsland()) Type.VOID else null

        "Stronghold",
        "The Wasteland", // TODO check if we can remove this
        "Smoldering Tomb",
        -> Type.INFERNO

        "Stillgore Château",
        "Oubliette",
        -> Type.VAMPIRE

        else -> null
    }

    private enum class SlayerLinesSource {
        NONE,
        SCOREBOARD,
        TAB,
    }

    private data class ParsedSlayer(
        val type: Type,
        val category: String,
        val tier: Int,
        val progress: String,
    )

    private class SlayerParseException(message: String) : Exception(message)
}
