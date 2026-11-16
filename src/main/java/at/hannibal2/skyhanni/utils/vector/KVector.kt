package at.hannibal2.skyhanni.utils.vector

import net.minecraft.core.Position
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3
import org.joml.Vector3fc

open class KVector(x: Double, y: Double, z: Double) : KVectorBase(x + 0.0, y + 0.0, z + 0.0) {

    constructor(x: Number, y: Number, z: Number) : this(x.toDouble(), y.toDouble(), z.toDouble())
    constructor(vec: Position) : this(vec.x(), vec.y(), vec.z())
    constructor(vec: Vec3i) : this(vec.x, vec.y, vec.z)
    constructor(vec: Vector3fc) : this(vec.x(), vec.y(), vec.z())

    override fun add(x: Double, y: Double, z: Double) = KVector(this.x + x, this.y + y, this.z + z)
    fun add(x: Number = 0, y: Number = 0, z: Number = 0) = add(x.toDouble(), y.toDouble(), z.toDouble())
    fun up(offset: Number = 1) = add(y = offset)
    fun down(offset: Number = 1) = up(-offset.toDouble())

    override fun scale(factor: Double) = KVector(x * factor, y * factor, z * factor)
    override fun normalize() = KVector(super.normalize())
    override fun lerp(other: Vec3, factor: Double) = KVector(super.lerp(other, factor))
    override fun cross(other: Vec3) = KVector(super.cross(other))

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
