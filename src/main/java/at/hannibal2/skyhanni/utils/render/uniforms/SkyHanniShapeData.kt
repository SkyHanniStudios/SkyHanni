package at.hannibal2.skyhanni.utils.render.uniforms

import at.hannibal2.skyhanni.test.command.ErrorManager
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.systems.RenderPass
import net.minecraft.client.renderer.DynamicUniformStorage
import java.nio.ByteBuffer
import java.nio.ByteOrder

object SkyHanniShapeData {
    const val UNIFORM_NAME = "SkyHanniShapeData"
    const val CAPACITY_DEFINE = "SHAPE_DATA_VEC4S"
    const val CAPACITY_VEC4S = 1024
    private const val VEC4_FLOATS = 4
    private const val CAPACITY_FLOATS = CAPACITY_VEC4S * VEC4_FLOATS
    private const val BLOCK_BYTES = CAPACITY_FLOATS * Float.SIZE_BYTES

    private val values = FloatArray(CAPACITY_FLOATS)
    private val storage by lazy { DynamicUniformStorage<ShapeBlock>("SkyHanni Shape Data UBO", BLOCK_BYTES, 1) }
    private var size = 0
    private var slice: GpuBufferSlice? = null
    private var overflowed = false

    fun add(params: FloatArray): Int? {
        require(params.size % VEC4_FLOATS == 0) { "Shape params must be whole vec4s, got ${params.size} floats" }
        if (size + params.size > CAPACITY_FLOATS) {
            reportOverflow(params.size / VEC4_FLOATS)
            return null
        }
        params.copyInto(values, size)
        return (size / VEC4_FLOATS).also { size += params.size }
    }

    fun upload() {
        if (slice != null || size == 0) return
        slice = storage.writeUniform(ShapeBlock())
    }

    fun bind(renderPass: RenderPass) {
        slice?.let { renderPass.setUniform(UNIFORM_NAME, it) }
    }

    fun endFrame() {
        if (slice != null) storage.endFrame()
        size = 0
        slice = null
        overflowed = false
    }

    private fun reportOverflow(requestedVec4s: Int) {
        if (overflowed) return
        overflowed = true
        ErrorManager.logErrorStateWithData(
            "Some SkyHanni GUI shapes were not drawn",
            "Shape data buffer full",
            "capacity vec4s" to CAPACITY_VEC4S,
            "used vec4s" to size / VEC4_FLOATS,
            "requested vec4s" to requestedVec4s,
        )
    }

    private class ShapeBlock : DynamicUniformStorage.DynamicUniform {
        override fun write(buffer: ByteBuffer) {
            buffer.order(ByteOrder.nativeOrder()).asFloatBuffer().put(values, 0, size)
        }
    }
}
