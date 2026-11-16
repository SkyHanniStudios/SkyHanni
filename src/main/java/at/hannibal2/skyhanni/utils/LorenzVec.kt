package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.vector.KVector
import at.hannibal2.skyhanni.utils.vector.VectorConversions.offsetVector
import at.hannibal2.skyhanni.utils.vector.VectorConversions.positionVector
import at.hannibal2.skyhanni.utils.vector.VectorConversions.previousPositionVector
import at.hannibal2.skyhanni.utils.vector.VectorConversions.toKVector
import at.hannibal2.skyhanni.utils.vector.VectorFormat.toKVector
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

@Deprecated("Use Direction.getUnitVec3")
val lorenzVecDirections = setOf(
    KVector(1, 0, 0),
    KVector(-1, 0, 0),
    KVector(0, 1, 0),
    KVector(0, -1, 0),
    KVector(0, 0, 1),
    KVector(0, 0, -1),
)

@Deprecated("Use toKVector from VectorFormat", ReplaceWith("toKVector()"))
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
