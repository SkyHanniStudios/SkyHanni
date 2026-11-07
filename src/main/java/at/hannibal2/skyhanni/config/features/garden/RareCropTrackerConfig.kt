package at.hannibal2.skyhanni.config.features.garden

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.config.features.misc.tracker.TopLevelTrackerConfig
import at.hannibal2.skyhanni.config.features.misc.tracker.generic.GardenTrackerSettings
import at.hannibal2.skyhanni.config.features.misc.tracker.individual.PerTrackerConfig
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class RareCropTrackerConfig : TopLevelTrackerConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Track rare crop drops while farming.")
    @ConfigEditorBoolean
    @FeatureToggle
    override var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Hide Chat", desc = "Hide the chat message when receiving a rare crop drop.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hideChat: Boolean = false

    @Expose
    @ConfigOption(name = "Max Lines", desc = "Maximum number of drops to show. Set to 0 to show all.")
    @ConfigEditorSlider(minValue = 0f, maxValue = 21f, minStep = 1f)
    var maxDisplayLines: Int = 5

    @Expose
    @ConfigOption(name = "Tracker Settings", desc = "")
    @Accordion
    override val perTrackerConfig: PerTrackerConfig<GardenTrackerSettings> = PerTrackerConfig()

    @Expose
    @ConfigLink(owner = RareCropTrackerConfig::class, field = "enabled")
    override val position: Position = Position(16, -232)
}
