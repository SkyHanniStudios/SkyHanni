package at.hannibal2.skyhanni.utils.vector

import net.minecraft.core.BlockPos
import net.minecraft.core.Rotations
import net.minecraft.core.Vec3i
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object VectorConversions {

    fun Vec3.toBlockPos(): BlockPos = BlockPos.containing(x, y, z)
    fun Vec3.toKVector() = this as? KVector ?: KVector(this)
    fun Vec3i.toKVector() = KVector(this)
    fun Rotations.toKVector() = KVector(x(), y(), z())

    fun ClientboundLevelParticlesPacket.toKVector() = KVector(x, y, z)
    fun ClientboundLevelParticlesPacket.offsetVector() = KVector(xDist, yDist, zDist)

    fun Entity.positionVector() = position().toKVector()
    fun Entity.previousPositionVector() = KVector(xOld, yOld, zOld)
}
