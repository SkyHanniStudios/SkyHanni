package at.hannibal2.skyhanni.config.features.garden.leaderboards.generics

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

open class EliteDisplayGenericConfig {

    @Expose
    @ConfigOption(
        name = "Text",
        desc = "Drag text to change the appearance of the overlay.\n",
    )
    @ConfigEditorDraggableList
    val text: Property<MutableList<LeaderboardTextEntry>> = Property.of(
        mutableListOf(
            LeaderboardTextEntry.WEIGHT_POSITION,
            LeaderboardTextEntry.OVERTAKE,
        ),
    )

    @Expose
    @ConfigOption(
        name = "Leaderboard Ranking",
        desc = "Show your position on the current leaderboard. Updates periodically.",
    )
    @ConfigEditorBoolean
    val leaderboard: Property<Boolean> = Property.of(true)

    @Expose
    @ConfigOption(name = "Show Outside Garden", desc = "Show this display outside the garden.")
    @ConfigEditorBoolean
    var showOutsideGarden: Boolean = false


    // While it would be nice to specify this per class, doing it this way makes it a lot easier to work with
    enum class LeaderboardTextEntry(private val displayName: String) {
        WEIGHT_POSITION("§6Leaderboard: §eAmount §r[§b#Rank§r]"),
        OVERTAKE("§eAmount §r(§bTime§r) §rbehind §bPlayer"),
        LAST_PLAYER("§eAmount §rahead of §bPlayer"),
        ;

        override fun toString() = displayName
    }
}
