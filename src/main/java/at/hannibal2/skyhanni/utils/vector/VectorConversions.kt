package at.hannibal2.skyhanni.utils.vector

import net.minecraft.core.Rotations
import net.minecraft.core.Vec3i
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object VectorConversions {

    fun Vec3.toKVector() = this as? KVector ?: KVector(this)
    fun Vec3i.toKVector() = KVector(this)
    fun Rotations.toKVector() = KVector(x(), y(), z())

    fun ClientboundLevelParticlesPacket.toKVector() = KVector(x, y, z)
    fun ClientboundLevelParticlesPacket.offsetVector() = KVector(xDist, yDist, zDist)

    fun Entity.positionVector() = position().toKVector()
    fun Entity.previousPositionVector() = KVector(xOld, yOld, zOld)

    fun Iterable<Double>.toKVector(): KVector {
        val size = count()
        require(size == 3) { "Can not transform $size values to KVector" }
        val values = iterator()
        return KVector(values.next(), values.next(), values.next())
    }

    fun parseStored(string: String): KVector {
        val (x, y, z) = string.split(":").map { it.toDouble() }
        return KVector(x, y, z)
    }

    fun parseStoredList(text: String) = text.split("\n").map { parseStored(it.replace("\"", "").replace(",", "")) }

    fun parseStoredOrNull(string: String): KVector? {
        val parts = string.split(":")
        if (parts.size != 3) return null
        val (x, y, z) = parts.map { it.trim().toDoubleOrNull() ?: return null }
        return KVector(x, y, z)
    }
}
