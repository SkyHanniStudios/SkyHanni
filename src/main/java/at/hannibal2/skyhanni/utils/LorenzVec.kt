package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.vector.KVector
import at.hannibal2.skyhanni.utils.vector.KVectorFormat
import at.hannibal2.skyhanni.utils.vector.VectorConversions.offsetVector
import at.hannibal2.skyhanni.utils.vector.VectorConversions.positionVector
import at.hannibal2.skyhanni.utils.vector.VectorConversions.previousPositionVector
import at.hannibal2.skyhanni.utils.vector.VectorConversions.toKVector
import net.minecraft.core.BlockPos
import net.minecraft.core.Rotations
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

@Deprecated(
    "Use KVector",
    ReplaceWith("KVector", "at.hannibal2.skyhanni.utils.vector.KVector"),
)
typealias LorenzVec = KVector

abstract class KVectorDeprecated(x: Double, y: Double, z: Double) : KVectorFormat(x, y, z) {

    @Deprecated("Use distanceTo", ReplaceWith("distanceTo(other)"))
    fun distance(other: Vec3) = distanceTo(other)
    @Deprecated("Use distanceToSqr", ReplaceWith("distanceToSqr(other)"))
    fun distanceSq(other: Vec3) = distanceToSqr(other)
    @Deprecated("Use lengthSqr", ReplaceWith("lengthSqr()"))
    fun lengthSquared() = lengthSqr()
    @Deprecated("Use dot", ReplaceWith("dot(other)"))
    fun dotProduct(other: Vec3) = dot(other)
    @Deprecated("Use cross", ReplaceWith("cross(other)"))
    fun crossProduct(other: Vec3) = cross(other)
    @Deprecated("Use unaryMinus", ReplaceWith("-this"))
    fun negated() = -this
    @Deprecated("Use rotateAroundY", ReplaceWith("rotateAroundY(theta)"))
    fun rotateXZ(theta: Double) = rotateAroundY(theta)
    @Deprecated("Use lerp", ReplaceWith("lerp(other, factor)"))
    fun slope(other: Vec3, factor: Double) = lerp(other, factor)
    @Deprecated("Use lerp", ReplaceWith("lerp(other, factor)"))
    fun interpolate(other: Vec3, factor: Double): KVector {
        require(factor in 0.0..1.0) { "Percentage must be between 0 and 1: $factor" }
        return lerp(other, factor)
    }
    @Deprecated("Use lerp", ReplaceWith("lerp(other, 0.5)"))
    fun middle(other: Vec3) = lerp(other, 0.5)
    @Deprecated("Use floor", ReplaceWith("floor()"))
    fun roundToBlock() = floor()
    @Deprecated("Use the vector itself", ReplaceWith("this"))
    fun clone() = KVector(this)
    @Deprecated("Use floor().down()")
    fun roundLocation(): KVector {
        val x = if (x < 0) x.toInt() - 1 else x.toInt()
        val y = y.toInt() - 1
        val z = if (z < 0) z.toInt() - 1 else z.toInt()
        return KVector(x, y, z)
    }
    @Deprecated("Use the vector itself", ReplaceWith("this"))
    fun toVec3(): Vec3 = this
    @Deprecated("Use formatRounded", ReplaceWith("formatRounded(accuracy, splitChar)"))
    fun printWithAccuracy(accuracy: Int, splitChar: String = " ") = formatRounded(accuracy, splitChar)
    @Deprecated("Use boxAround", ReplaceWith("boxAround(expand)"))
    fun boundingCenter(expand: Double) = boxAround(expand)
    @Deprecated("Use boxFromOrigin", ReplaceWith("boxFromOrigin(offX, offY, offZ)"))
    fun boundingToOffset(offX: Double, offY: Double, offZ: Double) = boxFromOrigin(offX, offY, offZ)
    @Deprecated("Use boxTo", ReplaceWith("boxTo(other)"))
    fun axisAlignedTo(other: Vec3) = boxTo(other)
    @Deprecated("Use nearestPointOnSegment", ReplaceWith("nearestPointOnSegment(startPos, endPos)"))
    fun nearestPointOnLine(startPos: Vec3, endPos: Vec3) = nearestPointOnSegment(startPos, endPos)
}

@Deprecated("Use toKVector from VectorConversions", ReplaceWith("toKVector()"))
fun List<Double>.toLorenzVec() = toKVector()

@Deprecated("Use toKVector from VectorConversions", ReplaceWith("toKVector()"))
fun BlockPos.toLorenzVec() = toKVector()

@Deprecated("Use toKVector from VectorConversions", ReplaceWith("toKVector()"))
fun Vec3.toLorenzVec() = toKVector()

@Deprecated("Use toKVector from VectorConversions", ReplaceWith("toKVector()"))
fun Rotations.toLorenzVec() = toKVector()

@Deprecated("Use toKVector from VectorConversions", ReplaceWith("toKVector()"))
fun ClientboundLevelParticlesPacket.toLorenzVec() = toKVector()

@Deprecated("Use offsetVector from VectorConversions", ReplaceWith("offsetVector()"))
fun ClientboundLevelParticlesPacket.toOffset() = offsetVector()

@Deprecated("Use positionVector from VectorConversions", ReplaceWith("positionVector()"))
fun Entity.getLorenzVec() = positionVector()

@Deprecated("Use previousPositionVector from VectorConversions", ReplaceWith("previousPositionVector()"))
fun Entity.getPrevLorenzVec() = previousPositionVector()

@Deprecated("Use AABB.inflate", ReplaceWith("inflate(vec.x, vec.y, vec.z)"))
fun AABB.expand(vec: KVector): AABB = inflate(vec.x, vec.y, vec.z)

@Deprecated("Use AABB.inflate", ReplaceWith("inflate(amount)"))
fun AABB.expand(amount: Double): AABB = inflate(amount)
