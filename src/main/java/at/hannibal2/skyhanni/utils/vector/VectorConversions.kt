package at.hannibal2.skyhanni.utils.vector

import net.minecraft.core.BlockPos
import net.minecraft.core.Rotations
import net.minecraft.core.Vec3i
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object VectorConversions {

    fun Vec3.toBlockPos(): BlockPos = BlockPos.containing(x, y, z)
    fun Vec3.toSkyHanniVector() = this as? SkyHanniVector ?: SkyHanniVector(this)
    fun Vec3i.toSkyHanniVector() = SkyHanniVector(x, y, z)
    fun Rotations.toSkyHanniVector() = SkyHanniVector(x(), y(), z())

    fun ClientboundLevelParticlesPacket.toSkyHanniVector() = SkyHanniVector(x, y, z)
    fun ClientboundLevelParticlesPacket.offsetVector() = SkyHanniVector(xDist, yDist, zDist)

    fun Entity.positionVector() = position().toSkyHanniVector()
    fun Entity.previousPositionVector() = SkyHanniVector(xOld, yOld, zOld)
}
