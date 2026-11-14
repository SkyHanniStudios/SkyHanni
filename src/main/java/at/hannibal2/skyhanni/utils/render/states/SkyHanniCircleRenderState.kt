package at.hannibal2.skyhanni.utils.render.states

import at.hannibal2.skyhanni.utils.render.SkyHanniRenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.navigation.ScreenRectangle

class SkyHanniCircleRenderState(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val color: Int,
    private val smoothness: Float,
    private val angle1: Float,
    private val angle2: Float,
    params: RoundedRenderStateParams,
    scissor: ScreenRectangle?,
) : AbstractSkyHanniRoundedShapeRenderState(x, y, width, height, params, scissor) {

    override val padding = 5
    override fun pipeline() = SkyHanniRenderPipeline.CIRCLE_DEFERRED()

    override fun shapeParams() = roundedParams(smoothness, angle1, angle2)

    override fun writeVertex(vertex: VertexConsumer, u: Float, v: Float) {
        vertex.setColor(color)
    }
}
