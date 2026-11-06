package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import at.hannibal2.skyhanni.utils.vector.VectorConversions.offsetVector
import at.hannibal2.skyhanni.utils.vector.VectorConversions.positionVector
import at.hannibal2.skyhanni.utils.vector.VectorConversions.previousPositionVector
import at.hannibal2.skyhanni.utils.vector.VectorConversions.toSkyHanniVector
import at.hannibal2.skyhanni.utils.vector.VectorFormat.toSkyHanniVector
import net.minecraft.core.BlockPos
import net.minecraft.core.Rotations
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

@Deprecated(
    "Use SkyHanniVector",
    ReplaceWith("SkyHanniVector", "at.hannibal2.skyhanni.utils.vector.SkyHanniVector"),
)
typealias LorenzVec = SkyHanniVector

@Deprecated("Use SkyHanniVector.ZERO", ReplaceWith("SkyHanniVector.ZERO", "at.hannibal2.skyhanni.utils.vector.SkyHanniVector"))
fun LorenzVec() = SkyHanniVector.ZERO

@Deprecated("Use VectorBlocks.AXIS_DIRECTIONS")
val lorenzVecDirections = setOf(
    SkyHanniVector(1, 0, 0),
    SkyHanniVector(-1, 0, 0),
    SkyHanniVector(0, 1, 0),
    SkyHanniVector(0, -1, 0),
    SkyHanniVector(0, 0, 1),
    SkyHanniVector(0, 0, -1),
)

@Deprecated("Use toSkyHanniVector from VectorFormat", ReplaceWith("toSkyHanniVector()"))
fun List<Double>.toLorenzVec() = toSkyHanniVector()

@Deprecated("Use toSkyHanniVector from VectorFormat", ReplaceWith("toDoubleArray().toSkyHanniVector()"))
fun Array<Double>.toLorenzVec() = toDoubleArray().toSkyHanniVector()

@Deprecated("Use toSkyHanniVector from VectorConversions", ReplaceWith("toSkyHanniVector()"))
fun BlockPos.toLorenzVec() = toSkyHanniVector()

@Deprecated("Use toSkyHanniVector from VectorConversions", ReplaceWith("toSkyHanniVector()"))
fun Vec3.toLorenzVec() = toSkyHanniVector()

@Deprecated("Use toSkyHanniVector from VectorConversions", ReplaceWith("toSkyHanniVector()"))
fun Rotations.toLorenzVec() = toSkyHanniVector()

@Deprecated("Use toSkyHanniVector from VectorConversions", ReplaceWith("toSkyHanniVector()"))
fun ClientboundLevelParticlesPacket.toLorenzVec() = toSkyHanniVector()

@Deprecated("Use offsetVector from VectorConversions", ReplaceWith("offsetVector()"))
fun ClientboundLevelParticlesPacket.toOffset() = offsetVector()

@Deprecated("Use positionVector from VectorConversions", ReplaceWith("positionVector()"))
fun Entity.getLorenzVec() = positionVector()

@Deprecated("Use previousPositionVector from VectorConversions", ReplaceWith("previousPositionVector()"))
fun Entity.getPrevLorenzVec() = previousPositionVector()

@Deprecated("Use AABB.inflate", ReplaceWith("inflate(vec.x, vec.y, vec.z)"))
fun AABB.expand(vec: SkyHanniVector): AABB = inflate(vec.x, vec.y, vec.z)

@Deprecated("Use AABB.inflate", ReplaceWith("inflate(amount)"))
fun AABB.expand(amount: Double): AABB = inflate(amount)
