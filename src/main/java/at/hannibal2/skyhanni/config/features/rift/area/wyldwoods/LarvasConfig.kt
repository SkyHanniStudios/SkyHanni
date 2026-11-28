package at.hannibal2.skyhanni.config.features.rift.area.wyldwoods

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class LarvasConfig {
    @Expose
    @ConfigOption(
        name = "Highlight",
        desc = "Highlight §cLarvas on trees §rwhile holding a §eLarva Hook §rin the hand."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    val highlight: Property<Boolean> = Property.of(true)

    @Expose
    @ConfigOption(name = "Color", desc = "Color of the Larvas.")
    @ConfigEditorColour
    var highlightColor: ChromaColour = ChromaColour.fromStaticRGB(13, 49, 255, 120)
}
