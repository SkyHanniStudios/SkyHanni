package at.hannibal2.skyhanni.config.features.minion

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.config.features.misc.tracker.TopLevelTrackerConfig
import at.hannibal2.skyhanni.config.features.misc.tracker.generic.ItemTrackerSettings
import at.hannibal2.skyhanni.config.features.misc.tracker.individual.PerTrackerConfig
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class InfernoProfitTrackerConfig : TopLevelTrackerConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Track items collected from Inferno Minions.")
    @ConfigEditorBoolean
    @FeatureToggle
    override var enabled: Boolean = true

    @Expose
    @ConfigLink(owner = InfernoProfitTrackerConfig::class, field = "enabled")
    override val position: Position = Position(250, 250)

    @Expose
    @ConfigOption(
        name = "Show After Collection",
        desc = "Show the tracker for a few seconds after collecting from an Inferno Minion.",
    )
    @ConfigEditorBoolean
    var showAfterCollection: Boolean = true

    @Expose
    @ConfigOption(name = "Tracker Settings", desc = "")
    @Accordion
    override val perTrackerConfig: PerTrackerConfig<ItemTrackerSettings> = PerTrackerConfig()
}
