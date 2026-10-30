package at.hannibal2.skyhanni.utils.vector

import at.hannibal2.skyhanni.utils.vector.VectorBlocks.blockCenter
import at.hannibal2.skyhanni.utils.vector.VectorBounds.boxAround
import at.hannibal2.skyhanni.utils.vector.VectorBounds.boxFromOrigin
import at.hannibal2.skyhanni.utils.vector.VectorBounds.boxTo
import at.hannibal2.skyhanni.utils.vector.VectorBounds.nearestPointOnSegment
import at.hannibal2.skyhanni.utils.vector.VectorConversions.toBlockPos
import at.hannibal2.skyhanni.utils.vector.VectorConversions.toVec3
import at.hannibal2.skyhanni.utils.vector.VectorFormat.asStoredString
import at.hannibal2.skyhanni.utils.vector.VectorFormat.formatRounded
import at.hannibal2.skyhanni.utils.vector.VectorFormat.toChatFormat
import at.hannibal2.skyhanni.utils.vector.VectorFormat.toCleanString
import at.hannibal2.skyhanni.utils.vector.VectorFormat.toLocalFormat
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

interface LegacyVectorMath<V : VectorMetrics<V>> : VectorMetrics<V> {

    @Deprecated("Use distance", ReplaceWith("distance(of(x, y, z))"))
    fun distance(x: Double, y: Double, z: Double) = distance(of(x, y, z))

    @Deprecated("Use distanceSq", ReplaceWith("distanceSq(of(x, y, z))"))
    fun distanceSq(x: Double, y: Double, z: Double) = distanceSq(of(x, y, z))

    @Deprecated("Use dot", ReplaceWith("this dot other"))
    fun dotProduct(other: V) = this dot other

    @Deprecated("Use cross", ReplaceWith("this cross other"))
    fun crossProduct(other: V) = this cross other

    @Deprecated("Use normalized", ReplaceWith("normalized()"))
    fun normalize() = normalized()

    @Deprecated("Use reciprocal", ReplaceWith("reciprocal()"))
    fun inverse() = reciprocal()

    @Deprecated("Use minComponent", ReplaceWith("minComponent()"))
    fun min() = minComponent()

    @Deprecated("Use maxComponent", ReplaceWith("maxComponent()"))
    fun max() = maxComponent()

    @Deprecated("Use elementwiseMin", ReplaceWith("elementwiseMin(other)"))
    fun minOfEachElement(other: V) = elementwiseMin(other)

    @Deprecated("Use elementwiseMax", ReplaceWith("elementwiseMax(other)"))
    fun maxOfEachElement(other: V) = elementwiseMax(other)

    @Deprecated("Use times", ReplaceWith("this * scalar"))
    fun scale(scalar: Double) = this * scalar

    @Deprecated("Use unaryMinus", ReplaceWith("-this"))
    fun negated() = -this

    @Deprecated("Use rotateAroundY", ReplaceWith("rotateAroundY(theta)"))
    fun rotateXZ(theta: Double) = rotateAroundY(theta)

    @Deprecated("Use lerp", ReplaceWith("lerp(other, factor)"))
    fun slope(other: V, factor: Double) = lerp(other, factor)

    @Deprecated("Use lerp", ReplaceWith("lerp(other, factor)"))
    fun interpolate(other: V, factor: Double) = lerp(other, factor)

    @Deprecated("Use midpoint", ReplaceWith("midpoint(other)"))
    fun middle(other: V) = midpoint(other)

    @Deprecated("Use floor", ReplaceWith("floor()"))
    fun roundToBlock() = floor()

    @Deprecated("Vectors are immutable, copying is never needed", ReplaceWith("this"))
    fun clone() = of(x, y, z)
}

interface LegacyVectorHelpers<V : VectorMetrics<V>> : VectorMetrics<V> {

    private val base: VectorMetrics<V> get() = this

    @Deprecated("Use toVec3 from VectorConversions", ReplaceWith("toVec3()"))
    fun toVec3(): Vec3 = base.toVec3()

    @Deprecated("Use toBlockPos from VectorConversions", ReplaceWith("toBlockPos()"))
    fun toBlockPos(): BlockPos = base.toBlockPos()

    @Deprecated("Use toChatFormat from VectorFormat", ReplaceWith("toChatFormat()"))
    fun toChatFormat() = base.toChatFormat()

    @Deprecated("Use toLocalFormat from VectorFormat", ReplaceWith("toLocalFormat()"))
    fun toLocalFormat() = base.toLocalFormat()

    @Deprecated("Use toCleanString from VectorFormat", ReplaceWith("toCleanString(separator)"))
    fun toCleanString(separator: String = ", ") = base.toCleanString(separator)

    @Deprecated("Use asStoredString from VectorFormat", ReplaceWith("asStoredString()"))
    fun asStoredString() = base.asStoredString()

    @Deprecated("Use formatRounded from VectorFormat", ReplaceWith("formatRounded(accuracy, splitChar)"))
    fun printWithAccuracy(accuracy: Int, splitChar: String = " ") = base.formatRounded(accuracy, splitChar)

    @Deprecated("Use blockCenter from VectorBlocks", ReplaceWith("blockCenter()"))
    fun blockCenter() = base.blockCenter()

    @Deprecated("Use boxAround from VectorBounds", ReplaceWith("boxAround(expand)"))
    fun boundingCenter(expand: Double): AABB = base.boxAround(expand)

    @Deprecated("Use boxFromOrigin from VectorBounds", ReplaceWith("boxFromOrigin(offX, offY, offZ)"))
    fun boundingToOffset(offX: Double, offY: Double, offZ: Double): AABB = base.boxFromOrigin(offX, offY, offZ)

    @Deprecated("Use boxTo from VectorBounds", ReplaceWith("boxTo(other)"))
    fun axisAlignedTo(other: V): AABB = base.boxTo(other)

    @Deprecated("Use nearestPointOnSegment from VectorBounds", ReplaceWith("nearestPointOnSegment(startPos, endPos)"))
    fun nearestPointOnLine(startPos: V, endPos: V) = base.nearestPointOnSegment(startPos, endPos)

    @Deprecated("Use floor().down(). Results differ at exact negative integer x or z, and at negative y.")
    fun roundLocation(): V {
        val x = if (x < 0) x.toInt() - 1 else x.toInt()
        val y = y.toInt() - 1
        val z = if (z < 0) z.toInt() - 1 else z.toInt()
        return of(x.toDouble(), y.toDouble(), z.toDouble())
    }
}
