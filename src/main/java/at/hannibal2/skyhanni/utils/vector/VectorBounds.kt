package at.hannibal2.skyhanni.utils.vector

import at.hannibal2.skyhanni.utils.vector.VectorOperators.div
import at.hannibal2.skyhanni.utils.vector.VectorOperators.minus
import at.hannibal2.skyhanni.utils.vector.VectorOperators.plus
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

    fun AABB.minCorner() = SkyHanniVector(minX, minY, minZ)
    fun AABB.maxCorner() = SkyHanniVector(maxX, maxY, maxZ)
    fun AABB.center() = (minCorner() + maxCorner()) / 2
    fun AABB.topCenter() = center().up((maxY - minY) / 2)
    fun AABB.inflate(vector: Vec3): AABB = inflate(vector.x, vector.y, vector.z)
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
}
