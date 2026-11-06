package at.hannibal2.skyhanni.utils.render.states

import at.hannibal2.skyhanni.utils.render.SkyHanniRenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.resources.Identifier

class SkyHanniRoundedTexturedRectRenderState(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    params: RoundedRenderStateParams,
    private val smoothness: Float,
    private val texture: Identifier,
    private val alpha: Float,
    scissor: ScreenRectangle?,
) : AbstractSkyHanniRoundedShapeRenderState(x, y, width, height, params, scissor) {

    override val padding = 5
    override fun pipeline() = SkyHanniRenderPipeline.ROUNDED_TEXTURED_RECT_DEFERRED()

    override fun shapeParams() = roundedParams(smoothness, alpha)

    override fun textureSetup(): TextureSetup {
        val fetchTexture = Minecraft.getInstance().textureManager.getTexture(texture)
        val view = fetchTexture.textureView
        return TextureSetup.singleTexture(view, fetchTexture.sampler)
    }

    override fun writeVertex(vertex: VertexConsumer, u: Float, v: Float) {
        vertex.setUv(u, v)
    }
}
