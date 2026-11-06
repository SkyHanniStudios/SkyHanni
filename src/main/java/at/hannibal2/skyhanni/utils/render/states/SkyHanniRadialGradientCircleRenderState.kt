package at.hannibal2.skyhanni.utils.render.states

import at.hannibal2.skyhanni.utils.render.SkyHanniRenderPipeline
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

    override fun shapeParams() =
        roundedParams(smoothness) + floatArrayOf(angle, progress, phaseOffset, if (reverse) 1f else 0f) + startColor + endColor
}
