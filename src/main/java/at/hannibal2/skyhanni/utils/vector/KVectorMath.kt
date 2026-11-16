package at.hannibal2.skyhanni.utils.vector

import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

abstract class KVectorMath(x: Double, y: Double, z: Double) : Vec3(x, y, z) {

    override fun add(x: Double, y: Double, z: Double) = KVector(this.x + x, this.y + y, this.z + z)
    fun add(x: Number = 0, y: Number = 0, z: Number = 0) = add(x.toDouble(), y.toDouble(), z.toDouble())
    fun up(offset: Number = 1) = add(y = offset)
    fun down(offset: Number = 1) = up(-offset.toDouble())
    fun addHalf() = add(0.5, 0.5, 0.5)

    override fun scale(factor: Double) = KVector(x * factor, y * factor, z * factor)
    override fun normalize() = KVector(super.normalize())
    override fun lerp(other: Vec3, factor: Double) = KVector(super.lerp(other, factor))
    override fun cross(other: Vec3) = KVector(super.cross(other))

    operator fun plus(other: Vec3) = KVector(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3) = KVector(x - other.x, y - other.y, z - other.z)
    operator fun times(other: Vec3) = KVector(x * other.x, y * other.y, z * other.z)
    operator fun times(scalar: Number) = scale(scalar.toDouble())
    operator fun div(other: Vec3) = KVector(x / other.x, y / other.y, z / other.z)
    operator fun div(scalar: Number) = scalar.toDouble().let { KVector(x / it, y / it, z / it) }
    operator fun unaryMinus() = KVector(-x, -y, -z)

    fun isZero() = x == 0.0 && y == 0.0 && z == 0.0
    fun isNormalized(tolerance: Double = 0.01) = abs(lengthSqr() - 1.0) < tolerance

    fun rotateAroundY(theta: Double): KVector {
        val c = cos(theta)
        val s = sin(theta)
        return KVector(x * c + z * s, y, -x * s + z * c)
    }
}
