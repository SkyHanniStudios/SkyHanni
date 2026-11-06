package at.hannibal2.skyhanni.utils.render.states

import at.hannibal2.skyhanni.utils.render.SkyHanniRenderPipeline
import at.hannibal2.skyhanni.mixins.hooks.GuiRendererHook
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

    private val roundedUniformIndex = with(params) {
        GuiRendererHook.addRoundedUniform(
            floatArrayOf(radius, borderThickness, adjustedHalfSizeX, adjustedHalfSizeY),
            floatArrayOf(adjustedCenterPosX, adjustedCenterPosY, borderBlur, 0f),
        )
    }

    override fun writeVertex(consumer: VertexConsumer, vx: Float, vy: Float, isTop: Boolean) {
        with(params) {
        consumer.addVertex(matXScale * vx + matXTranslation, matYScale * vy + matYTranslation, roundedUniformIndex.toFloat())
            .setColor(if (isTop) topColor else bottomColor)
        }
    }
}
