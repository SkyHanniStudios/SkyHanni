package at.hannibal2.skyhanni.test.renderable

import at.hannibal2.skyhanni.features.chroma.ChromaManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.chat.TextHelper
import at.hannibal2.skyhanni.utils.compat.appendWithColor
import at.hannibal2.skyhanni.utils.compat.componentBuilder
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.primitives.text

@SkyHanniModule(devOnly = true)
object TestChromaText : RenderableTestSuite.TestRenderable("chroma_text") {

    override fun renderable(): Renderable {
        ChromaManager.config.enabled.set(true)
        return Renderable.text(
            componentBuilder {
                appendWithColor("This is a chroma text", TextHelper.chromaStyle)
                append(" and this is normal text")
            }
        )
    }
}
