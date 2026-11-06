package at.hannibal2.skyhanni.utils.render.states

import at.hannibal2.skyhanni.utils.render.SkyHanniRenderPipeline
import at.hannibal2.skyhanni.mixins.hooks.GuiRendererHook
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

    private val roundedUniformIndex = with(params) {
        GuiRendererHook.addRoundedUniform(
            floatArrayOf(radius, smoothness, adjustedHalfSizeX, adjustedHalfSizeY),
            floatArrayOf(adjustedCenterPosX, adjustedCenterPosY, angle1, angle2),
        )
    }

    override fun writeVertex(consumer: VertexConsumer, vx: Float, vy: Float, isTop: Boolean) {
        with(params) {
        consumer.addVertex(matXScale * vx + matXTranslation, matYScale * vy + matYTranslation, roundedUniformIndex.toFloat())
            .setColor(color)
        }
    }
}
