package at.hannibal2.skyhanni.utils.vector

import net.minecraft.core.Direction
import net.minecraft.world.phys.AABB

object VectorBounds {

    operator fun Vector3<*>.get(axis: Direction.Axis): Double = axis.choose(x, y, z)

    fun <V : Vector3<V>> Vector3<V>.with(axis: Direction.Axis, value: Double) = when (axis) {
        Direction.Axis.X -> of(value, y, z)
        Direction.Axis.Y -> of(x, value, z)
        Direction.Axis.Z -> of(x, y, value)
    }

    fun Vector3<*>.boxAround(radius: Double) = AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius)

    fun Vector3<*>.boxTo(other: Vector3<*>) = AABB(x, y, z, other.x, other.y, other.z)

    fun Vector3<*>.boxFromOrigin(sizeX: Double, sizeY: Double, sizeZ: Double) =
        AABB(x, y, z, x + sizeX, y + sizeY, z + sizeZ)

    fun Vector3<*>.blockBox() = boxFromOrigin(1.0, 1.0, 1.0)

    fun SkyHanniBlockVector.box() = toVector().blockBox()

    fun AABB.minCorner() = SkyHanniVector(minX, minY, minZ)

    fun AABB.maxCorner() = SkyHanniVector(maxX, maxY, maxZ)

    fun AABB.edgeLengths() = maxCorner() - minCorner()

    fun AABB.center() = minCorner().midpoint(maxCorner())

    fun AABB.topCenter() = center().up((maxY - minY) / 2)

    fun AABB.contains(vector: Vector3<*>) = contains(vector.x, vector.y, vector.z)

    fun AABB.inflate(vector: Vector3<*>): AABB = inflate(vector.x, vector.y, vector.z)

    fun AABB.cornersAtHeight(y: Double) = listOf(
        SkyHanniVector(minX, y, minZ),
        SkyHanniVector(minX, y, maxZ),
        SkyHanniVector(maxX, y, maxZ),
        SkyHanniVector(maxX, y, minZ),
    )

    fun AABB.edges(): Set<Pair<SkyHanniVector, SkyHanniVector>> {
        val bottom = cornersAtHeight(minY)
        val top = cornersAtHeight(maxY)
        val ring = { corners: List<SkyHanniVector> -> corners.indices.map { corners[it] to corners[(it + 1) % corners.size] } }
        return (ring(bottom) + ring(top) + bottom.zip(top)).toSet()
    }

    fun <V : VectorMetrics<V>> VectorMetrics<V>.nearestPointOnSegment(start: V, end: V): V {
        val direction = end - start
        val lengthSquared = direction.lengthSquared()
        if (lengthSquared == 0.0) return start
        val t = ((self - start) dot direction / lengthSquared).coerceIn(0.0, 1.0)
        return start + direction * t
    }
}
