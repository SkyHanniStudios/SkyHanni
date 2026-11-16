package at.hannibal2.skyhanni.utils.vector

import at.hannibal2.skyhanni.utils.vector.VectorConversions.toKVector
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object VectorBounds {

    fun Vec3.blockBox() = boxFromOrigin(1, 1, 1)
    fun Vec3.boxAround(radius: Number) = radius.toDouble().let {
        AABB(x - it, y - it, z - it, x + it, y + it, z + it)
    }
    fun Vec3.boxTo(other: Vec3) =
        AABB(x, y, z, other.x, other.y, other.z)
    fun Vec3.boxFromOrigin(sizeX: Number, sizeY: Number, sizeZ: Number) =
        AABB(x, y, z, x + sizeX.toDouble(), y + sizeY.toDouble(), z + sizeZ.toDouble())

    fun AABB.inflate(vector: Vec3): AABB = inflate(vector.x, vector.y, vector.z)
    fun AABB.cornersAtHeight(y: Double) = listOf(
        KVector(minX, y, minZ),
        KVector(minX, y, maxZ),
        KVector(maxX, y, maxZ),
        KVector(maxX, y, minZ),
    )

    fun AABB.edges(): Set<Pair<KVector, KVector>> {
        val bottom = cornersAtHeight(minY)
        val top = cornersAtHeight(maxY)
        val ring = { corners: List<KVector> -> corners.indices.map { corners[it] to corners[(it + 1) % corners.size] } }
        return (ring(bottom) + ring(top) + bottom.zip(top)).toSet()
    }

    fun Vec3.nearestPointOnSegment(start: Vec3, end: Vec3): KVector {
        val direction = end.subtract(start)
        val lengthSqr = direction.lengthSqr()
        if (lengthSqr == 0.0) return start.toKVector()
        val t = (subtract(start).dot(direction) / lengthSqr).coerceIn(0.0, 1.0)
        return start.add(direction.scale(t)).toKVector()
    }
}
