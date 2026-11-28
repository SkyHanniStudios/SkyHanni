package at.hannibal2.skyhanni.utils.render.states

import at.hannibal2.skyhanni.utils.render.SkyHanniRenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.navigation.ScreenRectangle

class SkyHanniRoundedRectRenderState(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val color: Int,
    private val smoothness: Float,
    params: RoundedRenderStateParams,
    scissor: ScreenRectangle?,
    private val bottomColor: Int = color,
) : AbstractSkyHanniRoundedShapeRenderState(x, y, width, height, params, scissor) {

    override val padding = 5
    override fun pipeline() = SkyHanniRenderPipeline.ROUNDED_RECT_DEFERRED()

    override fun shapeParams() = roundedParams(smoothness)

    override fun writeVertex(vertex: VertexConsumer, u: Float, v: Float) {
        vertex.setColor(if (v == 0f) color else bottomColor)
    }
}
