package at.hannibal2.skyhanni.test.renderable

import at.hannibal2.skyhanni.features.chroma.ChromaManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.chat.TextHelper
import at.hannibal2.skyhanni.utils.compat.appendWithColor
import at.hannibal2.skyhanni.utils.compat.componentBuilder
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.container.VerticalContainerRenderable.Companion.vertical
import at.hannibal2.skyhanni.utils.renderables.primitives.text
import java.awt.Color

@SkyHanniModule(devOnly = true)
object TestText : RenderableTestSuite.TestRenderable("text") {

    override fun renderable(): Renderable {
        ChromaManager.config.enabled.set(true)
        return Renderable.vertical(
            Renderable.text(
                TextHelper.createGradientText(
                    Color.CYAN,
                    LorenzColor.LIGHT_PURPLE.toColor(),
                    "Really cool gradient that goes from aqua to pink",
                ),
            ),
            Renderable.text(
                componentBuilder {
                    appendWithColor("This is a chroma text", TextHelper.chromaStyle)
                    append(" and this is normal text")
                }
            ),
            Renderable.text(
                componentBuilder {
                    appendWithColor("This is transparent chroma text", TextHelper.chromaStyle)
                    append(" and this is not")
                },
                color = Color.YELLOW.addAlpha(100)
            ),
        )
    }
}
