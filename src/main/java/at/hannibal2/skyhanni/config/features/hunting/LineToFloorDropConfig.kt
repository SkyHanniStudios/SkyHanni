package at.hannibal2.skyhanni.config.features.hunting

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class LineToFloorDropConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Draws a line to the nearest Floor Drop on the ground.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = true

    @Expose
    @ConfigOption(name = "Line Width", desc = "The width of the line pointing to the nearest Floor Drop.")
    @ConfigEditorSlider(minStep = 1f, minValue = 1f, maxValue = 10f)
    var lineWidth: Int = 3

    @Expose
    @ConfigOption(name = "Islands", desc = "Select the islands where the line to Floor Drop should work.")
    @ConfigEditorDraggableList
    val islands: MutableList<FloorDropIsland> = mutableListOf(
        FloorDropIsland.MOONGLADE_MARSH,
        FloorDropIsland.TORRHUS_CANYON,
        FloorDropIsland.CRITTER_SAFARI,
    )

    enum class FloorDropIsland(val displayName: String) {
        MOONGLADE_MARSH("Moonglade Marsh"),
        TORRHUS_CANYON("Torrhus Canyon"),
        CRITTER_SAFARI("Critter Safari"),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(
        name = "Distance",
        desc = "Maximum distance between you and a Floor Drop for the line to be shown.",
    )
    @ConfigEditorSlider(minStep = 1f, minValue = 5f, maxValue = 128f)
    var maxDistance: Int = 15

    @Expose
    @ConfigOption(
        name = "Scan Radius",
        desc = "Radius in blocks to scan for Floor Drops around the player. Higher values may impact performance.",
    )
    @ConfigEditorSlider(minStep = 1f, minValue = 20f, maxValue = 64f)
    var scanRadius: Int = 20
}
