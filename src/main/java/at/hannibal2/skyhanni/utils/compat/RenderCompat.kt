package at.hannibal2.skyhanni.utils.compat

import com.mojang.renderpearl.api.pipeline.RenderPipeline
import net.minecraft.client.renderer.RenderPipelines

object RenderCompat {
    /**
     * The depth of an empty render target. 26.2 renders with a reversed depth range,
     * so "nothing drawn" is 0 rather than 1.
     */
    //~ if < 26.2 '0.0' -> '1.0'
    const val CLEAR_DEPTH = 0.0

    fun getMinecraftGuiTextured(): RenderPipeline = RenderPipelines.GUI_TEXTURED
}
