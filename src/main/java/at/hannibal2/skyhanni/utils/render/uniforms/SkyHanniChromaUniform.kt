package at.hannibal2.skyhanni.utils.render.uniforms

import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import net.minecraft.client.renderer.DynamicUniformStorage
import java.nio.ByteBuffer

class SkyHanniChromaUniform : AutoCloseable {
    companion object {
        const val UNIFORM_NAME = "SkyHanniChromaUniforms"
    }

    private val uniformSize = Std140SizeCalculator().putFloat().putFloat().putFloat().putInt().get()

    private val storage = DynamicUniformStorage<UniformValue>("SkyHanni Chroma UBO", uniformSize, 2)

    fun writeWith(
        chromaSize: Float,
        timeOffset: Float,
        saturation: Float,
        forwardDirection: Int,
    ): GpuBufferSlice = storage.writeUniform(UniformValue(chromaSize, timeOffset, saturation, forwardDirection))

    fun clear() = storage.endFrame()

    override fun close() = storage.close()

    data class UniformValue(
        val chromaSize: Float,
        val timeOffset: Float,
        val saturation: Float,
        val forwardDirection: Int,
    ) : DynamicUniformStorage.DynamicUniform {
        override fun write(buffer: ByteBuffer) {
            Std140Builder.intoBuffer(buffer)
                .putFloat(chromaSize)
                .putFloat(timeOffset)
                .putFloat(saturation)
                .putInt(forwardDirection)
        }
    }
}
