package at.hannibal2.skyhanni.utils.vector

import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

abstract class KVectorSpatial(x: Double, y: Double, z: Double) : KVectorMath(x, y, z) {

    fun floor() = KVector(floor(x), floor(y), floor(z))
    fun ceil() = KVector(ceil(x), ceil(y), ceil(z))
    fun roundTo(precision: Int) = KVector(x.roundTo(precision), y.roundTo(precision), z.roundTo(precision))
    fun blockCenter() = floor().addHalf()
    fun toBlockPos(): BlockPos = BlockPos.containing(x, y, z)

    fun equalsIgnoreY(other: Vec3) = x == other.x && z == other.z
    fun distanceSqIgnoreY(other: Vec3) = subtract(other).horizontalDistanceSqr()
    fun distanceIgnoreY(other: Vec3) = subtract(other).horizontalDistance()
    fun distanceChebyshevIgnoreY(other: Vec3) = max(abs(x - other.x), abs(z - other.z))

    fun blockBox() = boxFromOrigin(1, 1, 1)
    fun boxAround(radius: Number) = radius.toDouble().let {
        AABB(x - it, y - it, z - it, x + it, y + it, z + it)
    }
    fun boxTo(other: Vec3) = AABB(x, y, z, other.x, other.y, other.z)
    fun boxFromOrigin(sizeX: Number, sizeY: Number, sizeZ: Number) =
        AABB(x, y, z, x + sizeX.toDouble(), y + sizeY.toDouble(), z + sizeZ.toDouble())

    fun nearestPointOnSegment(start: Vec3, end: Vec3): KVector {
        val direction = end.subtract(start)
        val lengthSqr = direction.lengthSqr()
        if (lengthSqr == 0.0) return KVector(start)
        val t = (subtract(start).dot(direction) / lengthSqr).coerceIn(0.0, 1.0)
        return KVector(start.add(direction.scale(t)))
    }
}
