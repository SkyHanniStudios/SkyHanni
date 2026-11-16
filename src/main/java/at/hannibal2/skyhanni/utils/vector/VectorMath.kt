package at.hannibal2.skyhanni.utils.vector

import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin

object VectorMath {

    fun Vec3.floor() = KVector(floor(x), floor(y), floor(z))
    fun Vec3.ceil() = KVector(ceil(x), ceil(y), ceil(z))
    fun Vec3.roundTo(precision: Int) = KVector(x.roundTo(precision), y.roundTo(precision), z.roundTo(precision))
    fun Vec3.addHalf() = KVector(x + 0.5, y + 0.5, z + 0.5)
    fun Vec3.blockCenter() = floor().addHalf()

    fun Vec3.toDoubleArray() = doubleArrayOf(x, y, z)
    fun Vec3.isZero() = x == 0.0 && y == 0.0 && z == 0.0
    fun Vec3.isNormalized(tolerance: Double = 0.01) = abs(lengthSqr() - 1.0) < tolerance
    fun Vec3.equalsIgnoreY(other: Vec3) = x == other.x && z == other.z
    fun Vec3.distanceSqIgnoreY(other: Vec3) = subtract(other).horizontalDistanceSqr()
    fun Vec3.distanceIgnoreY(other: Vec3) = subtract(other).horizontalDistance()
    fun Vec3.distanceChebyshevIgnoreY(other: Vec3) = max(abs(x - other.x), abs(z - other.z))

    fun Vec3.rotateAroundY(theta: Double): KVector {
        val c = cos(theta)
        val s = sin(theta)
        return KVector(x * c + z * s, y, -x * s + z * c)
    }
}
