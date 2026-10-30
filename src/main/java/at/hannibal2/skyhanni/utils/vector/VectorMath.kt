package at.hannibal2.skyhanni.utils.vector

import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.vector.VectorConversions.toSkyHanniVector
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object VectorMath {

    fun Vec3.floor() = SkyHanniVector(floor(x), floor(y), floor(z))
    fun Vec3.ceil() = SkyHanniVector(ceil(x), ceil(y), ceil(z))
    fun Vec3.roundTo(precision: Int) = SkyHanniVector(x.roundTo(precision), y.roundTo(precision), z.roundTo(precision))
    fun Vec3.addHalf() = SkyHanniVector(x + 0.5, y + 0.5, z + 0.5)
    fun Vec3.blockCenter() = floor().addHalf()
    fun Vec3.reciprocal() = SkyHanniVector(1.0 / x, 1.0 / y, 1.0 / z)

    fun Vec3.elementwiseMin(other: Vec3) = SkyHanniVector(min(x, other.x), min(y, other.y), min(z, other.z))
    fun Vec3.elementwiseMax(other: Vec3) = SkyHanniVector(max(x, other.x), max(y, other.y), max(z, other.z))
    fun Vec3.minComponent() = min(x, min(y, z))
    fun Vec3.maxComponent() = max(x, max(y, z))

    fun Vec3.toDoubleArray() = doubleArrayOf(x, y, z)
    fun Vec3.isZero() = x == 0.0 && y == 0.0 && z == 0.0
    fun Vec3.isNormalized(tolerance: Double = 0.01) = abs(lengthSqr() - 1.0) < tolerance
    fun Vec3.equalsIgnoreY(other: Vec3) = x == other.x && z == other.z
    fun Vec3.distanceSqIgnoreY(other: Vec3) = subtract(other).horizontalDistanceSqr()
    fun Vec3.distanceIgnoreY(other: Vec3) = subtract(other).horizontalDistance()
    fun Vec3.distanceChebyshevIgnoreY(other: Vec3) = max(abs(x - other.x), abs(z - other.z))

    fun Vec3.nearestPointOnSegment(start: Vec3, end: Vec3): SkyHanniVector {
        val direction = end.subtract(start)
        val lengthSqr = direction.lengthSqr()
        if (lengthSqr == 0.0) return start.toSkyHanniVector()
        val t = (subtract(start).dot(direction) / lengthSqr).coerceIn(0.0, 1.0)
        return start.add(direction.scale(t)).toSkyHanniVector()
    }
}
