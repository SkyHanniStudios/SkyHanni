package at.hannibal2.skyhanni.config.features.slayer

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class WolfConfig {

    @Expose
    @ConfigOption(
        name = "Highlight Boss",
        desc = "Highlight your Packmaster Soul. When it vanishes mid-leap," +
            " the wolf is shown once it moves faster than a normal wolf.",
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var highlight: Boolean = true

    @Expose
    @ConfigOption(name = "Highlight Color", desc = "Color of the boss highlight.")
    @ConfigEditorColour
    var highlightColor: ChromaColour = ChromaColour.fromStaticRGB(0, 255, 88, 255)

    @Expose
    @ConfigOption(name = "Draw Line", desc = "Draw a line from your crosshair to the boss (or the wolf it possesses).")
    @ConfigEditorBoolean
    @FeatureToggle
    var drawLine: Boolean = false

    @Expose
    @ConfigOption(name = "Line Color", desc = "Color of the line.")
    @ConfigEditorColour
    var lineColor: ChromaColour = ChromaColour.fromStaticRGB(0, 255, 88, 255)

    @Expose
    @ConfigOption(name = "Line Width", desc = "Width of the line.")
    @ConfigEditorSlider(minStep = 1f, minValue = 1f, maxValue = 10f)
    var lineWidth: Int = 2

    @Expose
    @ConfigOption(name = "Bloodfrenzy Display", desc = "Show the Bloodfrenzy stacks of your boss in a GUI element.")
    @ConfigEditorBoolean
    @FeatureToggle
    var bloodfrenzyDisplay: Boolean = true

    @Expose
    @ConfigLink(owner = WolfConfig::class, field = "bloodfrenzyDisplay")
    val bloodfrenzyPosition: Position = Position(10, 200)

    @Expose
    @ConfigOption(
        name = "Bloodfrenzy Alert",
        desc = "Show a title once your boss reaches the Bloodfrenzy stacks set below.",
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var bloodfrenzyAlert: Boolean = false

    @Expose
    @ConfigOption(name = "Alert Stacks", desc = "Bloodfrenzy stacks needed for the alert.")
    @ConfigEditorSlider(minStep = 1f, minValue = 1f, maxValue = 20f)
    var bloodfrenzyAlertStacks: Int = 5

    @Expose
    @ConfigOption(name = "Mute Wolf Repellent", desc = "Mutes the sound of the Wolf Repellent.")
    @ConfigEditorBoolean
    @FeatureToggle
    var muteWolfRepellent: Boolean = false
}
