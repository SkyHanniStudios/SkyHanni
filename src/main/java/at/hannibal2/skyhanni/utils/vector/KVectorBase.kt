package at.hannibal2.skyhanni.utils.vector

import at.hannibal2.skyhanni.utils.vector.VectorBounds.boxAround
import at.hannibal2.skyhanni.utils.vector.VectorBounds.boxFromOrigin
import at.hannibal2.skyhanni.utils.vector.VectorBounds.boxTo
import at.hannibal2.skyhanni.utils.vector.VectorBounds.nearestPointOnSegment
import at.hannibal2.skyhanni.utils.vector.VectorConversions.toBlockPos
import at.hannibal2.skyhanni.utils.vector.VectorFormat.asStoredString
import at.hannibal2.skyhanni.utils.vector.VectorFormat.formatRounded
import at.hannibal2.skyhanni.utils.vector.VectorFormat.toChatFormat
import at.hannibal2.skyhanni.utils.vector.VectorFormat.toCleanString
import at.hannibal2.skyhanni.utils.vector.VectorFormat.toLocalFormat
import at.hannibal2.skyhanni.utils.vector.VectorMath.addHalf
import at.hannibal2.skyhanni.utils.vector.VectorMath.blockCenter
import at.hannibal2.skyhanni.utils.vector.VectorMath.ceil
import at.hannibal2.skyhanni.utils.vector.VectorMath.distanceChebyshevIgnoreY
import at.hannibal2.skyhanni.utils.vector.VectorMath.distanceIgnoreY
import at.hannibal2.skyhanni.utils.vector.VectorMath.distanceSqIgnoreY
import at.hannibal2.skyhanni.utils.vector.VectorMath.equalsIgnoreY
import at.hannibal2.skyhanni.utils.vector.VectorMath.floor
import at.hannibal2.skyhanni.utils.vector.VectorMath.isNormalized
import at.hannibal2.skyhanni.utils.vector.VectorMath.isZero
import at.hannibal2.skyhanni.utils.vector.VectorMath.rotateAroundY
import at.hannibal2.skyhanni.utils.vector.VectorMath.roundTo
import at.hannibal2.skyhanni.utils.vector.VectorMath.toDoubleArray
import at.hannibal2.skyhanni.utils.vector.VectorOperators.copy
import at.hannibal2.skyhanni.utils.vector.VectorOperators.div
import at.hannibal2.skyhanni.utils.vector.VectorOperators.minus
import at.hannibal2.skyhanni.utils.vector.VectorOperators.plus
import at.hannibal2.skyhanni.utils.vector.VectorOperators.times
import at.hannibal2.skyhanni.utils.vector.VectorOperators.unaryMinus
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

sealed class KVectorDeprecated(x: Double, y: Double, z: Double) : Vec3(x, y, z) {

    protected val vec: Vec3 get() = this

    @Deprecated("Use distanceTo", ReplaceWith("distanceTo(other)"))
    fun distance(other: Vec3) = distanceTo(other)
    @Deprecated("Use distanceToSqr", ReplaceWith("distanceToSqr(other)"))
    fun distanceSq(other: Vec3) = distanceToSqr(other)
    @Deprecated("Use lengthSqr", ReplaceWith("lengthSqr()"))
    fun lengthSquared() = lengthSqr()
    @Deprecated("Use dot", ReplaceWith("dot(other)"))
    fun dotProduct(other: Vec3) = dot(other)
    @Deprecated("Use cross", ReplaceWith("cross(other)"))
    fun crossProduct(other: Vec3) = KVector(cross(other))
    @Deprecated("Use unaryMinus from VectorOperators", ReplaceWith("-this"))
    fun negated() = -vec
    @Deprecated("Use rotateAroundY from VectorMath", ReplaceWith("rotateAroundY(theta)"))
    fun rotateXZ(theta: Double) = vec.rotateAroundY(theta)
    @Deprecated("Use lerp", ReplaceWith("lerp(other, factor)"))
    fun slope(other: Vec3, factor: Double) = KVector(lerp(other, factor))
    @Deprecated("Use lerp", ReplaceWith("lerp(other, factor)"))
    fun interpolate(other: Vec3, factor: Double) = KVector(lerp(other, factor))
    @Deprecated("Use lerp", ReplaceWith("lerp(other, 0.5)"))
    fun middle(other: Vec3) = KVector(lerp(other, 0.5))
    @Deprecated("Use floor from VectorMath", ReplaceWith("floor()"))
    fun roundToBlock() = vec.floor()
    @Deprecated("Vectors are immutable, copying is never needed", ReplaceWith("this"))
    fun clone() = KVector(this)
    @Deprecated("Use floor().down(). Results differ at exact negative integer x or z, and at negative y.")
    fun roundLocation(): KVector {
        val x = if (x < 0) x.toInt() - 1 else x.toInt()
        val y = y.toInt() - 1
        val z = if (z < 0) z.toInt() - 1 else z.toInt()
        return KVector(x, y, z)
    }
    @Deprecated("KVector is a Vec3", ReplaceWith("this"))
    fun toVec3(): Vec3 = this
    @Deprecated("Use formatRounded from VectorFormat", ReplaceWith("formatRounded(accuracy, splitChar)"))
    fun printWithAccuracy(accuracy: Int, splitChar: String = " ") = vec.formatRounded(accuracy, splitChar)
    @Deprecated("Use boxAround from VectorBounds", ReplaceWith("boxAround(expand)"))
    fun boundingCenter(expand: Double): AABB = vec.boxAround(expand)
    @Deprecated("Use boxFromOrigin from VectorBounds", ReplaceWith("boxFromOrigin(offX, offY, offZ)"))
    fun boundingToOffset(offX: Double, offY: Double, offZ: Double): AABB = vec.boxFromOrigin(offX, offY, offZ)
    @Deprecated("Use boxTo from VectorBounds", ReplaceWith("boxTo(other)"))
    fun axisAlignedTo(other: Vec3): AABB = vec.boxTo(other)
    @Deprecated("Use nearestPointOnSegment from VectorBounds", ReplaceWith("nearestPointOnSegment(startPos, endPos)"))
    fun nearestPointOnLine(startPos: Vec3, endPos: Vec3) = vec.nearestPointOnSegment(startPos, endPos)
}

open class KVectorHelpers(x: Double, y: Double, z: Double) : KVectorDeprecated(x, y, z) {

    fun toBlockPos(): BlockPos = vec.toBlockPos()
    fun toChatFormat() = vec.toChatFormat()
    fun toLocalFormat() = vec.toLocalFormat()
    fun toCleanString(separator: String = ", ") = vec.toCleanString(separator)
    fun asStoredString() = vec.asStoredString()

    fun floor() = vec.floor()
    fun ceil() = vec.ceil()
    fun roundTo(precision: Int) = vec.roundTo(precision)
    fun addHalf() = vec.addHalf()
    fun blockCenter() = vec.blockCenter()
    fun toDoubleArray() = vec.toDoubleArray()
    fun isZero() = vec.isZero()
    fun isNormalized(tolerance: Double = 0.01) = vec.isNormalized(tolerance)
}

open class KVectorBase(x: Double, y: Double, z: Double) : KVectorHelpers(x, y, z) {

    operator fun component1() = x
    operator fun component2() = y
    operator fun component3() = z
    fun copy(x: Number = this.x, y: Number = this.y, z: Number = this.z) = vec.copy(x, y, z)

    operator fun plus(other: Vec3) = vec + other
    operator fun minus(other: Vec3) = vec - other
    operator fun times(other: Vec3) = vec * other
    operator fun times(scalar: Number) = vec * scalar
    operator fun div(scalar: Number) = vec / scalar
    operator fun div(other: Vec3) = KVector(x / other.x, y / other.y, z / other.z)
    operator fun unaryMinus() = -vec

    fun equalsIgnoreY(other: Vec3) = vec.equalsIgnoreY(other)
    fun distanceSqIgnoreY(other: Vec3) = vec.distanceSqIgnoreY(other)
    fun distanceIgnoreY(other: Vec3) = vec.distanceIgnoreY(other)
    fun distanceChebyshevIgnoreY(other: Vec3) = vec.distanceChebyshevIgnoreY(other)
}
