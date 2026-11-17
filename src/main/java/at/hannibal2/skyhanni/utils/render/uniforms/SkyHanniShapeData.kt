package at.hannibal2.skyhanni.utils.render.uniforms

import at.hannibal2.skyhanni.test.command.ErrorManager
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.system.MemoryStack
import java.util.Arrays

object SkyHanniShapeData {
    const val UNIFORM_NAME = "SkyHanniShapeData"
    const val CAPACITY_DEFINE = "SHAPE_DATA_VEC4S"
    const val CAPACITY_VEC4S = 1024
    private const val VEC4_FLOATS = 4
    private const val CAPACITY_FLOATS = CAPACITY_VEC4S * VEC4_FLOATS
    private const val BLOCK_BYTES = CAPACITY_FLOATS * Float.SIZE_BYTES

    private val values = FloatArray(CAPACITY_FLOATS)
    private val uploaded = FloatArray(CAPACITY_FLOATS)
    private val buffer by lazy {
        RenderSystem.getDevice().createBuffer(
            { "SkyHanni Shape Data UBO" },
            GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_COPY_DST,
            BLOCK_BYTES.toLong(),
        )
    }
    private var size = 0
    private var uploadedSize = 0
    private var uploadedThisFrame = false
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
        if (uploadedThisFrame || size == 0) return
        uploadedThisFrame = true
        if (size == uploadedSize && Arrays.equals(values, 0, size, uploaded, 0, size)) return
        MemoryStack.stackPush().use { stack ->
            val bytes = stack.malloc(size * Float.SIZE_BYTES)
            bytes.asFloatBuffer().put(values, 0, size)
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(0L, bytes.remaining().toLong()), bytes)
        }
        values.copyInto(uploaded, 0, 0, size)
        uploadedSize = size
    }

    fun bind(renderPass: RenderPass) {
        if (uploadedSize > 0) renderPass.setUniform(UNIFORM_NAME, buffer)
    }

    fun endFrame() {
        size = 0
        uploadedThisFrame = false
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
}
