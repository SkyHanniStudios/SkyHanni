package at.hannibal2.skyhanni.utils.vector

import at.hannibal2.skyhanni.utils.KVectorDeprecated
import net.minecraft.core.Position
import net.minecraft.core.Vec3i
import org.joml.Vector3fc

open class KVector(x: Double, y: Double, z: Double) : KVectorDeprecated(x + 0.0, y + 0.0, z + 0.0) {

    constructor(x: Number, y: Number, z: Number) : this(x.toDouble(), y.toDouble(), z.toDouble())
    constructor(vec: Position) : this(vec.x(), vec.y(), vec.z())
    constructor(vec: Vec3i) : this(vec.x, vec.y, vec.z)
    constructor(vec: Vector3fc) : this(vec.x(), vec.y(), vec.z())

    operator fun component1() = x
    operator fun component2() = y
    operator fun component3() = z
    fun copy(x: Number = this.x, y: Number = this.y, z: Number = this.z) = KVector(x, y, z)
    fun toDoubleArray() = doubleArrayOf(x, y, z)

    private fun matches(x: Number, y: Number, z: Number) =
        this.x == x.toDouble() && this.y == y.toDouble() && this.z == z.toDouble()

    override fun equals(other: Any?) = when (other) {
        is Position -> matches(other.x(), other.y(), other.z())
        is Vec3i -> matches(other.x, other.y, other.z)
        is Vector3fc -> matches(other.x(), other.y(), other.z())
        else -> false
    }

    override fun hashCode() = super.hashCode()

    companion object {
        val ZERO = KVector(0, 0, 0)
    }
}
