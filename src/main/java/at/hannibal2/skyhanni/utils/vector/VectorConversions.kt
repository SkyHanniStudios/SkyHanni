package at.hannibal2.skyhanni.utils.vector

import net.minecraft.core.BlockPos
import net.minecraft.core.Position
import net.minecraft.core.Rotations
import net.minecraft.core.Vec3i
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import org.joml.Vector3fc

object VectorConversions {

    fun Vector3<*>.toVec3() = Vec3(x, y, z)

    fun Vector3<*>.toVector3f() = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())

    fun Vector3<*>.toBlockPos(): BlockPos = BlockPos.containing(x, y, z)

    fun SkyHanniBlockVector.toBlockPos(): BlockPos = BlockPos.of(asLong())

    fun Position.toSkyHanniVector() = SkyHanniVector(x(), y(), z())

    fun Vec3i.toSkyHanniVector() = SkyHanniVector(x, y, z)

    fun BlockPos.toSkyHanniBlockVector() = SkyHanniBlockVector.fromPacked(asLong())

    fun Vector3fc.toSkyHanniVector() = SkyHanniVector(x().toDouble(), y().toDouble(), z().toDouble())

    fun Rotations.toSkyHanniVector() = SkyHanniVector(x().toDouble(), y().toDouble(), z().toDouble())

    fun ClientboundLevelParticlesPacket.toSkyHanniVector() = SkyHanniVector(x, y, z)

    fun ClientboundLevelParticlesPacket.offsetVector() =
        SkyHanniVector(xDist.toDouble(), yDist.toDouble(), zDist.toDouble())

    fun Entity.positionVector() = position().toSkyHanniVector()

    fun Entity.previousPositionVector() = SkyHanniVector(xOld, yOld, zOld)

    fun Entity.blockVector() = blockPosition().toSkyHanniBlockVector()

    fun Vec3.isEqualTo(other: Vec3) = x == other.x && y == other.y && z == other.z
}
