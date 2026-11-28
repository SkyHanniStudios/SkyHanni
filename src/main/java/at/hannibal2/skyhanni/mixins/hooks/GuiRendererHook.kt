package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.minecraftevents.ClientEvents
import at.hannibal2.skyhanni.config.features.chroma.ChromaConfig.Direction
import at.hannibal2.skyhanni.features.chroma.ChromaManager
import at.hannibal2.skyhanni.utils.compat.GuiScreenUtils
import at.hannibal2.skyhanni.utils.render.SkyHanniRenderPipeline
import at.hannibal2.skyhanni.utils.render.item.SkyHanniItemRenderCoordinator
import at.hannibal2.skyhanni.utils.render.uniforms.SkyHanniChromaUniform
import at.hannibal2.skyhanni.utils.render.uniforms.SkyHanniShapeData
import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderPass
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph.GlyphInstance
import net.minecraft.client.renderer.state.gui.GlyphRenderState
import net.minecraft.client.renderer.state.gui.GuiElementRenderState

object GuiRendererHook {
    private val chromaUniform = SkyHanniChromaUniform()
    private var chromaBufferSlice: GpuBufferSlice? = null

    @JvmStatic
    fun beginFrame() {
        chromaBufferSlice = writeChromaUniform()
    }

    @JvmStatic
    fun endFrame() {
        SkyHanniItemRenderCoordinator.endFrame()
        SkyHanniShapeData.endFrame()
        chromaUniform.clear()
        chromaBufferSlice = null
    }

    @JvmStatic
    fun bindUniforms(renderPass: RenderPass) {
        val chroma = chromaBufferSlice ?: writeChromaUniform().also { chromaBufferSlice = it }
        renderPass.setUniform(SkyHanniChromaUniform.UNIFORM_NAME, chroma)
        SkyHanniShapeData.bind(renderPass)
    }

    fun uploadShapeData() = SkyHanniShapeData.upload()

    private fun writeChromaUniform(): GpuBufferSlice {
        val config = ChromaManager.config
        val direction = config.chromaDirection
        val partialTicks = Minecraft.getInstance().deltaTracker.getGameTimeDeltaPartialTick(true)
        val ticks = (ClientEvents.totalTicks + partialTicks).let {
            when (direction) {
                Direction.FORWARD_RIGHT, Direction.BACKWARD_RIGHT -> it
                Direction.FORWARD_LEFT, Direction.BACKWARD_LEFT -> -it
            }
        }
        val forwardDirection = when (direction) {
            Direction.FORWARD_RIGHT, Direction.FORWARD_LEFT -> 1
            Direction.BACKWARD_RIGHT, Direction.BACKWARD_LEFT -> 0
        }
        return chromaUniform.writeWith(
            chromaSize = config.chromaSize * (GuiScreenUtils.displayWidth / 100f),
            timeOffset = ticks * (config.chromaSpeed / 360f),
            saturation = config.chromaSaturation,
            forwardDirection = forwardDirection,
        )
    }

    fun replacePipeline(state: GuiElementRenderState, original: Operation<RenderPipeline>): RenderPipeline {
        if (!SkyHanniMod.feature.gui.chroma.enabled.get() || state !is GlyphRenderState) return original.call(state)
        val glyphColor = (state.renderable as? GlyphInstance)?.style?.color ?: return original.call(state)
        return if (glyphColor.name == "chroma") SkyHanniRenderPipeline.CHROMA_TEXT() else original.call(state)
    }
}
