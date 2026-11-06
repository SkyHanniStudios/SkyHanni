package at.hannibal2.skyhanni.utils.render.states

import at.hannibal2.skyhanni.utils.render.uniforms.SkyHanniShapeData
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup

abstract class AbstractSkyHanniRoundedShapeRenderState(
    protected val x: Int,
    protected val y: Int,
    protected val width: Int,
    protected val height: Int,
    protected val params: RoundedRenderStateParams,
    private val scissor: ScreenRectangle?,
) : SkyHanniRenderState {

    protected abstract val padding: Int

    protected abstract fun shapeParams(): FloatArray

    override fun bounds(): ScreenRectangle = with(params) {
        ScreenRectangle(
            (matXScale * (x - padding) + matXTranslation).toInt(),
            (matYScale * (y - padding) + matYTranslation).toInt(),
            ((width + padding * 2) * matXScale).toInt(),
            ((height + padding * 2) * matYScale).toInt(),
        )
    }

    override fun scissorArea(): ScreenRectangle? = scissor
    override fun textureSetup(): TextureSetup = TextureSetup.noTexture()

    override fun buildVertices(consumer: VertexConsumer) {
        val index = SkyHanniShapeData.add(shapeParams())
        val p = padding.toFloat()
        val left = x - p
        val top = y - p
        val right = if (index == null) left else x + width + p
        val bottom = if (index == null) top else y + height + p
        val z = index?.toFloat() ?: 0f
        writeVertex(consumer.shapeVertex(left, top, z), u = 0f, v = 0f)
        writeVertex(consumer.shapeVertex(left, bottom, z), u = 0f, v = 1f)
        writeVertex(consumer.shapeVertex(right, bottom, z), u = 1f, v = 1f)
        writeVertex(consumer.shapeVertex(right, top, z), u = 1f, v = 0f)
    }

    protected open fun writeVertex(vertex: VertexConsumer, u: Float, v: Float) {}

    protected fun roundedParams(secondary: Float, z: Float = 0f, w: Float = 0f): FloatArray = with(params) {
        floatArrayOf(radius, secondary, adjustedHalfSizeX, adjustedHalfSizeY, adjustedCenterPosX, adjustedCenterPosY, z, w)
    }

    private fun VertexConsumer.shapeVertex(vx: Float, vy: Float, z: Float): VertexConsumer = with(params) {
        addVertex(matXScale * vx + matXTranslation, matYScale * vy + matYTranslation, z)
    }
}
