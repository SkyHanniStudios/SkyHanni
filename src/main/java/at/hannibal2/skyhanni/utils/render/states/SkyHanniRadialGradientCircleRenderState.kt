package at.hannibal2.skyhanni.utils.render.states

import at.hannibal2.skyhanni.utils.render.SkyHanniRenderPipeline
import at.hannibal2.skyhanni.mixins.hooks.GuiRendererHook
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.navigation.ScreenRectangle

class SkyHanniRadialGradientCircleRenderState(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    params: RoundedRenderStateParams,
    private val smoothness: Float,
    private val angle: Float,
    private val progress: Float,
    private val phaseOffset: Float,
    private val reverse: Boolean,
    private val startColor: FloatArray,
    private val endColor: FloatArray,
    scissor: ScreenRectangle?,
) : AbstractSkyHanniRoundedShapeRenderState(x, y, width, height, params, scissor) {

    override val padding = 5
    override fun pipeline() = SkyHanniRenderPipeline.RADIAL_GRADIENT_CIRCLE_DEFERRED()

    private val roundedUniformIndex = with(params) {
        GuiRendererHook.addRoundedUniform(
            floatArrayOf(radius, smoothness, adjustedHalfSizeX, adjustedHalfSizeY),
            floatArrayOf(adjustedCenterPosX, adjustedCenterPosY, 0f, 0f),
            floatArrayOf(angle, progress, phaseOffset, if (reverse) 1f else 0f),
            startColor,
            endColor,
        )
    }

    override fun buildVertices(consumer: VertexConsumer) {
        val p = padding.toFloat()
        writeGradientVertex(consumer, (x - p), (y - p))
        writeGradientVertex(consumer, (x - p), (y + height + p))
        writeGradientVertex(consumer, (x + width + p), (y + height + p))
        writeGradientVertex(consumer, (x + width + p), (y - p))
    }

    private fun writeGradientVertex(consumer: VertexConsumer, vx: Float, vy: Float) = with(params) {
        consumer.addVertex(matXScale * vx + matXTranslation, matYScale * vy + matYTranslation, roundedUniformIndex.toFloat())
    }
}
