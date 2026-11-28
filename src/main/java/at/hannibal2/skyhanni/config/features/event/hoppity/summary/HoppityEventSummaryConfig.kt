package at.hannibal2.skyhanni.config.features.event.hoppity.summary

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class HoppityEventSummaryConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Show a summary of your Hoppity's Hunt stats when the event is over.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = true

    @Expose
    @Accordion
    @ConfigOption(name = "Live Display", desc = "")
    val liveDisplay: HoppityLiveDisplayConfig = HoppityLiveDisplayConfig()

    @Expose
    @Accordion
    @ConfigOption(name = "Update CF Reminders", desc = "")
    val cfReminder: HoppityEventSummaryCFReminderConfig = HoppityEventSummaryCFReminderConfig()

    @Expose
    @ConfigLink(owner = HoppityEventSummaryConfig::class, field = "liveDisplay")
    val liveDisplayPosition: Position = Position(0, 0)

    @Expose
    @ConfigOption(name = "Stats List", desc = "Drag text to change what displays in the summary card.")
    @ConfigEditorDraggableList
    val statDisplayList: Property<MutableList<HoppityStat>> = Property.of(
        mutableListOf(
            HoppityStat.MEAL_EGGS_FOUND,
            HoppityStat.HOPPITY_RABBITS_BOUGHT,
            HoppityStat.SIDE_DISH_EGGS,
            HoppityStat.MILESTONE_RABBITS,
            HoppityStat.EMPTY_1,
            HoppityStat.NEW_RABBITS,
            HoppityStat.EMPTY_2,
            HoppityStat.DUPLICATE_RABBITS,
            HoppityStat.EMPTY_3,
            HoppityStat.TIME_IN_CF,
            HoppityStat.RABBIT_THE_FISH_FINDS,
            HoppityStat.LEADERBOARD_CHANGE,
        ),
    )

    @Suppress("MaxLineLength")
    enum class HoppityStat(private val displayName: String) {
        MEAL_EGGS_FOUND("§rYou found §b45§r/§a47 §6Chocolate Meal Eggs§r."),
        HITMAN_EGGS("§rYou recovered §b20§r/§a40 §rmissed §6Meal Eggs §rfrom §cRabbit Hitman§r."),
        HOPPITY_RABBITS_BOUGHT("§rYou bought §b7 §fRabbits §rfrom §aHoppity§r."),
        VISITOR_RABBITS("§6Hoppity §rgave you §b3 §dRabbits §ron your §aGarden§r."),
        SIDE_DISH_EGGS("§rYou found §b4 §6§lSide Dish Eggs §r§rin the §6Chocolate Factory§r."),
        MILESTONE_RABBITS("§rYou claimed §b2 §6§lMilestone Rabbits§r."),
        EMPTY_1(""),
        NEW_RABBITS("§rUnique Rabbits: §f7\n §f1 §r- §a1 §r- §91 §r- §51 §r- §61 §r- §d1 §r- §b1"),
        EMPTY_2(""),
        DUPLICATE_RABBITS("§rDuplicate Rabbits: §f10\n §f4 §r- §a3 §r- §92 §r- §51 §r- §60 §r- §d0 §r- §b0\n §6+250,000,000 Chocolate"),
        EMPTY_3(""),
        STRAY_RABBITS("§rStray Rabbits: §f20\n §f10 §r- §a6 §r- §93 §r- §51 §r- §60 §r- §d0 §r- §b0\n §6+8,000,000 Chocolate\n  §4* §c§oRequires Stray Tracker being enabled to work§4§o."),
        EMPTY_4(""),
        TIME_IN_CF("§rYou spent §b4h 36m §rin the §6Chocolate Factory§r."),
        RABBIT_THE_FISH_FINDS("§rYou found §cRabbit the Fish §rin Meal Eggs §b2 §rtimes."),
        LEADERBOARD_CHANGE("§rLeaderboard: §b#107 §c-> §b#99\n §r(§a+8 Spots§r) Top §a0.003% §c-> §rTop §a0.002%\n§4* §c§oSyncs on first §4§o/cf §c§orun per server swap§4§o."),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(
        name = "Show Count Diff",
        desc = "Store and display the count of Uniques/Duplicates next to their respective rabbit count.\n" +
            ".e.g. §rUnique Rabbits: §f5 §r(270 -> 275)",
    )
    @ConfigEditorBoolean
    var showCountDiff: Boolean = false
}
