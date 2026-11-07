package at.hannibal2.skyhanni.config.features.event

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.config.features.misc.tracker.TopLevelTrackerConfig
import at.hannibal2.skyhanni.config.features.misc.tracker.generic.TrackerSettings
import at.hannibal2.skyhanni.config.features.misc.tracker.individual.PerTrackerConfig
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.annotations.SearchTag

class BeachBallTrackerConfig : TopLevelTrackerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Track the §bBeach Balls §7you use and §dFishy Treats §7you earn.")
    @ConfigEditorBoolean
    @FeatureToggle
    @SearchTag("beach")
    override var enabled: Boolean = true

    @Expose
    @ConfigOption(
        name = "Hide After Inactivity",
        desc = "Hide the tracker after this many minutes without using a ball.",
    )
    @ConfigEditorSlider(minValue = 1.0f, maxValue = 30.0f, minStep = 1.0f)
    @SearchTag("beach")
    var hideAfterInactivity: Int = 5

    @Expose
    @ConfigOption(name = "Tracker Settings", desc = "")
    @Accordion
    override val perTrackerConfig: PerTrackerConfig<TrackerSettings> = PerTrackerConfig()

    @Expose
    @ConfigLink(owner = BeachBallTrackerConfig::class, field = "enabled")
    override val position: Position = Position(170, 170)
}
