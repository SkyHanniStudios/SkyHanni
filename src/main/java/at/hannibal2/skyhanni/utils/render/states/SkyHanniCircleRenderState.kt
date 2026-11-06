package at.hannibal2.skyhanni.utils.render.states

import at.hannibal2.skyhanni.utils.render.SkyHanniRenderPipeline
import at.hannibal2.skyhanni.utils.render.states.featureRenderer.SkyHanniFeatureSubmit
import com.mojang.blaze3d.PrimitiveTopology
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.renderer.DynamicUniformStorage
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher
import net.minecraft.client.renderer.state.gui.GuiRenderState
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState
import org.joml.Matrix3x2f
import org.joml.Matrix3x2fc
import org.joml.Vector2f
import java.nio.ByteBuffer

data class SkyHanniCircleRenderState(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val color: Int,
    val smoothness: Float,
    val angle1: Float,
    val angle2: Float,
    val params: RoundedRenderStateParams,
    val scissor: ScreenRectangle?,
    val pose: Matrix3x2f,
) : PictureInPictureRenderState {
    val rectangle = ScreenRectangle(x, y, width, height).transformAxisAligned(pose)

    override fun x0(): Int = x
    override fun x1(): Int = x + width
    override fun y0(): Int = y
    override fun y1(): Int = y + height

    override fun scale(): Float = 1f
    override fun pose(): Matrix3x2fc = pose
    override fun scissorArea(): ScreenRectangle? = scissor

    override fun bounds(): ScreenRectangle = rectangle
}

class SkyHanniCirclePipRenderer(ignored: PictureInPictureRendererRegistry.Context) : PictureInPictureRenderer<SkyHanniCircleRenderState>() {

    override fun getRenderStateClass(): Class<SkyHanniCircleRenderState> = SkyHanniCircleRenderState::class.java

    var lastState: SkyHanniCircleRenderState? = null

    override fun textureIsReadyToBlit(renderState: SkyHanniCircleRenderState): Boolean {
        return lastState == renderState
    }

    private var guiScale: Int = 1

    override fun prepare(
        renderState: SkyHanniCircleRenderState,
        guiRenderState: GuiRenderState,
        featureRenderDispatcher: FeatureRenderDispatcher,
        guiScale: Int,
    ) {
        this.guiScale = guiScale
        super.prepare(renderState, guiRenderState, featureRenderDispatcher, guiScale)
    }

    override fun renderToTexture(
        state: SkyHanniCircleRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
    ) = with(state) {
        lastState = state

        val uniform = SkyHanniCircleUniform(state, guiScale)
        submitNodeCollector.submitCustom(
            SubmitRenderPhases.ALWAYS_ON_TOP,
            SkyHanniFeatureSubmit(
                DefaultVertexFormat.POSITION_COLOR,
                PrimitiveTopology.QUADS,
                SkyHanniRenderPipeline.CIRCLE_DEFERRED(),
                -1,
                meshBuilder = {
                    addVertex(0f, 0f, 0f).setColor(color)
                    addVertex(0f, (height * guiScale).toFloat(), 0f).setColor(color)
                    addVertex((width * guiScale).toFloat(), (height * guiScale).toFloat(), 0f).setColor(color)
                    addVertex((width * guiScale).toFloat(), 0f, 0f).setColor(color)
                },
                setup = {
                    setUniform(SkyHanniCircleUniform.NAME, uniform.write())
                },
            ),
        )
    }

    override fun blitTexture(renderState: SkyHanniCircleRenderState, guiRenderState: GuiRenderState) {
        super.blitTexture(renderState, guiRenderState)
    }

    override fun getTextureLabel(): String = "SkyHanniCircleRenderState"
}

data class SkyHanniCircleUniform(
    val smoothness: Float,
    val halfWidth: Vector2f,
    val center: Vector2f,
    val angle1: Float,
    val angle2: Float,
) : DynamicUniformStorage.DynamicUniform {
    constructor(state: SkyHanniCircleRenderState, guiScale: Int) : this(
        state.smoothness,
        Vector2f((state.width / 2f) * guiScale, (state.height / 2f) * guiScale),
        Vector2f((state.width / 2f) * guiScale, (state.height / 2f) * guiScale),
        state.angle1,
        state.angle2,
    )

    companion object {
        val SIZE = Std140SizeCalculator()
            .putFloat() // smoothness
            .putVec2() // halfWidth
            .putVec2() // center
            .putFloat() // angle1
            .putFloat() // angle2
            .get()

        const val NAME: String = "SkyHanniRoundedUniforms"
        val storage = DynamicUniformStorage<SkyHanniCircleUniform>(NAME, SIZE, 1)

        @JvmStatic
        fun endFrame() = storage.endFrame()
    }

    fun write() = storage.writeUniform(this)

    override fun write(byteBuffer: ByteBuffer) {
        Std140Builder.intoBuffer(byteBuffer)
            .putFloat(this.smoothness)
            .putVec2(this.halfWidth)
            .putVec2(this.center)
            .putFloat(this.angle1)
            .putFloat(this.angle2)
            .get()
    }
}
