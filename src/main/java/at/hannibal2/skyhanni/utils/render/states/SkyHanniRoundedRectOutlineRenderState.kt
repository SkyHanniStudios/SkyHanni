package at.hannibal2.skyhanni.utils.render.states

import at.hannibal2.skyhanni.utils.render.SkyHanniRenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.navigation.ScreenRectangle

class SkyHanniRoundedRectOutlineRenderState(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val topColor: Int,
    private val bottomColor: Int,
    private val borderThickness: Float,
    private val borderBlur: Float,
    params: RoundedRenderStateParams,
    scissor: ScreenRectangle?,
) : AbstractSkyHanniRoundedShapeRenderState(x, y, width, height, params, scissor) {

    override val padding = (borderThickness / 2).toInt()
    override fun pipeline() = SkyHanniRenderPipeline.ROUNDED_RECT_OUTLINE_DEFERRED()

    override fun shapeParams() = roundedParams(borderThickness, borderBlur)

    override fun writeVertex(vertex: VertexConsumer, u: Float, v: Float) {
        vertex.setColor(if (v == 0f) topColor else bottomColor)
    }
}
