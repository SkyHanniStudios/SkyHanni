package at.hannibal2.skyhanni.utils.compat

import com.mojang.renderpearl.api.pipeline.RenderPipeline
import net.minecraft.client.renderer.RenderPipelines

//? if < 26.2 {
/*import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.renderpearl.api.commands.RenderPass
import com.mojang.renderpearl.api.device.GpuDevice
import java.util.OptionalDouble
import java.util.OptionalInt
*///?}

object RenderCompat {
    /**
     * The depth of an empty render target. 26.2 renders with a reversed depth range,
     * so "nothing drawn" is 0 rather than 1.
     */
    //~ if < 26.2 '0.0' -> '1.0'
    const val CLEAR_DEPTH = 0.0

    fun getMinecraftGuiTextured(): RenderPipeline = RenderPipelines.GUI_TEXTURED

    //? if < 26.2 {
    /*fun RenderPass.enableRenderPassScissorStateIfAble() {
        val scissorState = RenderSystem.getScissorStateForRenderTypeDraws()
        if (scissorState.enabled()) {
            this.enableScissor(scissorState.x(), scissorState.y(), scissorState.width(), scissorState.height())
        }
    }

    fun RenderPass.drawIndexed(indices: Int) {
        drawIndexed(
            0,
            0,
            indices,
            1,
        )
    }

    private fun RenderTarget.findColorAttachment() = this.colorTextureView

    private fun RenderTarget.findDepthAttachment() = if (this.useDepth) this.depthTextureView else null

    fun GpuDevice.createRenderPass(name: String, framebuffer: RenderTarget): RenderPass {
        val colorAttachment = framebuffer.findColorAttachment() ?: error("color attachment is null")
        return this.createCommandEncoder().createRenderPass(
            { name },
            colorAttachment,
            OptionalInt.empty(),
            framebuffer.findDepthAttachment(),
            OptionalDouble.empty(),
        )
    }
    *///?}
}
