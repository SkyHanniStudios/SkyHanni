package at.hannibal2.skyhanni.utils.render.uniforms

import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import net.minecraft.client.renderer.DynamicUniformStorage
import java.nio.ByteBuffer

class SkyHanniRoundedUniform : AutoCloseable {
    companion object {
        const val MAX_STATES = 256
        const val VEC4S_PER_STATE = 5
    }

    private val uniformSize = Std140SizeCalculator()
        .apply {
            repeat(MAX_STATES * VEC4S_PER_STATE * 4) {
                putFloat()
            }
        }
        .get()
    private val storage = DynamicUniformStorage<UniformValue>("SkyHanni Rounded Uniforms", uniformSize, 2)

    private val values = ArrayList<FloatArray>(MAX_STATES * VEC4S_PER_STATE)

    fun add(
        rounded0: FloatArray,
        rounded1: FloatArray,
        gradient0: FloatArray,
        startColor: FloatArray,
        endColor: FloatArray,
    ): Int {
        check(values.size < MAX_STATES * VEC4S_PER_STATE) { "Too many SkyHanni rounded render states" }
        val index = values.size / VEC4S_PER_STATE
        values.add(rounded0)
        values.add(rounded1)
        values.add(gradient0)
        values.add(startColor)
        values.add(endColor)
        return index
    }

    fun write(): GpuBufferSlice {
        val paddedValues = values + List(MAX_STATES * VEC4S_PER_STATE - values.size) {
            floatArrayOf(0f, 0f, 0f, 0f)
        }
        return storage.writeUniform(UniformValue(paddedValues))
    }

    fun clear() {
        values.clear()
        storage.endFrame()
    }
    override fun close() = storage.close()

    private data class UniformValue(
        val values: List<FloatArray>,
    ) : DynamicUniformStorage.DynamicUniform {
        override fun write(buffer: ByteBuffer) {
            val builder = Std140Builder.intoBuffer(buffer)
            values.forEach { value ->
                builder
                    .putFloat(value[0])
                    .putFloat(value[1])
                    .putFloat(value[2])
                    .putFloat(value[3])
            }
        }
    }
}
