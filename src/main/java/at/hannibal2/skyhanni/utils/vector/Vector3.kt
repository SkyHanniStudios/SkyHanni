package at.hannibal2.skyhanni.utils.vector

import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

interface Vector3<V : Vector3<V>> {
    val x: Double
    val y: Double
    val z: Double

    fun of(x: Double, y: Double, z: Double): V

    val self: V get() = of(x, y, z)

    fun toDoubleArray() = doubleArrayOf(x, y, z)

    fun isZero() = x == 0.0 && y == 0.0 && z == 0.0

    fun equalsIgnoreY(other: V) = x == other.x && z == other.z

    fun minComponent() = min(x, min(y, z))

    fun maxComponent() = max(x, max(y, z))

    fun elementwiseMin(other: V) = of(min(x, other.x), min(y, other.y), min(z, other.z))

    fun elementwiseMax(other: V) = of(max(x, other.x), max(y, other.y), max(z, other.z))

    fun floor() = of(floor(x), floor(y), floor(z))

    fun ceil() = of(ceil(x), ceil(y), ceil(z))

    fun roundTo(precision: Int) = of(x.roundTo(precision), y.roundTo(precision), z.roundTo(precision))
}

interface VectorArithmetic<V : VectorArithmetic<V>> : Vector3<V> {

    operator fun plus(other: V) = of(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: V) = of(x - other.x, y - other.y, z - other.z)

    operator fun times(other: V) = of(x * other.x, y * other.y, z * other.z)

    operator fun div(other: V) = of(x / other.x, y / other.y, z / other.z)

    operator fun times(scalar: Number) = scalar.toDouble().let { of(x * it, y * it, z * it) }

    operator fun div(scalar: Number) = scalar.toDouble().let { of(x / it, y / it, z / it) }

    operator fun unaryMinus() = of(-x, -y, -z)

    fun add(x: Number = 0, y: Number = 0, z: Number = 0) = of(this.x + x.toDouble(), this.y + y.toDouble(), this.z + z.toDouble())

    fun up(offset: Number = 1) = of(x, y + offset.toDouble(), z)

    fun down(offset: Number = 1) = of(x, y - offset.toDouble(), z)

    fun addHalf() = add(0.5, 0.5, 0.5)

    fun reciprocal() = of(1.0 / x, 1.0 / y, 1.0 / z)

    fun lerp(other: V, factor: Double) = this + (other - self) * factor

    fun midpoint(other: V) = lerp(other, 0.5)
}

interface VectorMetrics<V : VectorMetrics<V>> : VectorArithmetic<V> {

    infix fun dot(other: V) = x * other.x + y * other.y + z * other.z

    infix fun cross(other: V) = of(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x,
    )

    fun lengthSquared() = x * x + y * y + z * z

    fun length() = sqrt(lengthSquared())

    fun normalized() = this / length()

    fun isNormalized(tolerance: Double = 0.01) = (lengthSquared() - 1.0).absoluteValue < tolerance

    fun distanceSq(other: V) = (other - self).lengthSquared()

    fun distance(other: V) = sqrt(distanceSq(other))

    fun distanceSqIgnoreY(other: V): Double {
        val dx = other.x - x
        val dz = other.z - z
        return dx * dx + dz * dz
    }

    fun distanceIgnoreY(other: V) = sqrt(distanceSqIgnoreY(other))

    fun distanceChebyshevIgnoreY(other: V) = max(abs(x - other.x), abs(z - other.z))

    fun rotateAroundY(theta: Double): V {
        val c = cos(theta)
        val s = sin(theta)
        return of(x * c + z * s, y, -x * s + z * c)
    }
}
