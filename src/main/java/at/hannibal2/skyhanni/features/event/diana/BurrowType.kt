package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.utils.ColorUtils.rgb
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.compat.appendWithColor
import at.hannibal2.skyhanni.utils.compat.componentBuilder
import io.github.notenoughupdates.moulconfig.ChromaColour
import net.minecraft.network.chat.Component

private val config get() = SkyHanniMod.feature.event.diana.burrowCustomization

enum class BurrowType(val text: String, val color: () -> ChromaColour) {

    START(
        "Start",
        { config.startBurrowColor },
    ),
    MOB(
        "Mob",
        { config.mobBurrowColor },
    ),
    TREASURE(
        "Treasure",
        { config.treasureBurrowColor }
    ),
    UNKNOWN(
        "Unknown?!",
        { LorenzColor.WHITE.toChromaColor() }
    ),
    ;

    override fun toString() = text

    fun getBurrowText(): Component = componentBuilder {
        appendWithColor(this@BurrowType.text, this@BurrowType.color().rgb)
    }
}
