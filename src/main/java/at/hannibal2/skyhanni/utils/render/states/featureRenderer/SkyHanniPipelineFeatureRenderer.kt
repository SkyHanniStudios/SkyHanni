package at.hannibal2.skyhanni.utils.render.states.featureRenderer

import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import com.mojang.blaze3d.PrimitiveTopology
import com.mojang.blaze3d.opengl.Uniform
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.StagedVertexBuffer
import net.minecraft.client.renderer.feature.FeatureFrameContext
import net.minecraft.client.renderer.feature.FeatureRenderer
import net.minecraft.client.renderer.feature.FeatureRendererType
import net.minecraft.client.renderer.feature.submit.SubmitNode
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.util.ARGB
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import java.util.Optional

@SkyHanniModule
object SkyHanniPipelineFeatureRenderer : FeatureRenderer<SkyHanniFeatureSubmit> {
    val type: FeatureRendererType<SkyHanniFeatureSubmit> = FeatureRendererType.create("SkyHanniPipeline")

    init {
        FeatureRendererRegistry.register(type) { SkyHanniPipelineFeatureRenderer }
    }

    data class Group(val draw: StagedVertexBuffer.Draw, val submit: SkyHanniFeatureSubmit)

    val groups: MutableList<Group> = mutableListOf()

    override fun prepareGroup(
        context: FeatureFrameContext,
        submits: List<SkyHanniFeatureSubmit>,
        strictlyOrdered: Boolean,
    ) {
        val stagedVertexBuffer = context.stagedVertexBuffer
        submits.forEach { submit ->
            val draw = stagedVertexBuffer.appendDraw(submit.vertexFormat, submit.primitiveTopology)
            val builder = stagedVertexBuffer.getVertexBuilder(draw)
            submit.meshBuilder(builder)

            groups.add(Group(draw, submit))
        }
    }

    override fun executeGroup(
        context: FeatureFrameContext,
        groupIndex: Int,
        submits: List<SkyHanniFeatureSubmit>,
        strictlyOrdered: Boolean,
    ) {
        val stagedVertexBuffer = context.stagedVertexBuffer

        val device = RenderSystem.getDevice()
        val mainTarget = RenderSystem.outputColorTextureOverride ?: return
        device.createCommandEncoder().createRenderPass({ "" }, mainTarget, Optional.empty()).use { pass ->
            groups.forEach { group ->
                pass.pushDebugGroup { "SkyHanni Pipeline Feature Renderer Group - $group" }
                val executionInfo = stagedVertexBuffer.getExecuteInfo(group.draw) ?: return@forEach

                pass.setPipeline(group.submit.pipeline)

                RenderSystem.getScissorStateForRenderTypeDraws().takeIf { it.enabled() }?.let {
                    pass.enableScissor(it.x(), it.y(), it.width(), it.height())
                }

                val textures = group.submit.textures
                if (textures.texure0 != null) pass.bindTexture("Sampler0", textures.texure0, textures.sampler0)
                if (textures.texure1 != null) pass.bindTexture("Sampler1", textures.texure1, textures.sampler1)
                if (textures.texure2 != null) pass.bindTexture("Sampler2", textures.texure2, textures.sampler2)

                RenderSystem.bindDefaultUniforms(pass)
                val colorModulator = group.submit.colorModulator
                pass.setUniform("DynamicTransforms", RenderSystem.getDynamicUniforms().writeTransform(
                    RenderSystem.getModelViewMatrixCopy(),
                    Vector4f(ARGB.redFloat(colorModulator), ARGB.greenFloat(colorModulator), ARGB.blueFloat(colorModulator), ARGB.alphaFloat(colorModulator)),
                    Vector3f(),
                    Matrix4f()
                ))

                group.submit.setup(pass)

                pass.setVertexBuffer(0, executionInfo.vertexBuffer.slice())
                pass.setIndexBuffer(executionInfo.indexBuffer, executionInfo.indexType)

                pass.drawIndexed(executionInfo.indexCount, 1, executionInfo.firstIndex, executionInfo.baseVertex, 0)

                pass.popDebugGroup()
            }
        }
    }

    override fun finishExecute(context: FeatureFrameContext) {
        this.groups.clear()
    }

}

data class SkyHanniFeatureSubmit(
    val vertexFormat: VertexFormat,
    val primitiveTopology: PrimitiveTopology,
    val pipeline: RenderPipeline,

    val colorModulator: Int,
    val textures: TextureSetup = TextureSetup.noTexture(),

    val meshBuilder: VertexConsumer.() -> Unit,
    val setup: RenderPass.() -> Unit,
) : SubmitNode {
    override fun featureType(): FeatureRendererType<out SubmitNode> = SkyHanniPipelineFeatureRenderer.type

}
