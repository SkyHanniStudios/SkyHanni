package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import at.hannibal2.skyhanni.utils.compat.addRedstoneOres
import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import at.hannibal2.skyhanni.utils.vector.VectorConversions.toSkyHanniVector
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.SkullBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object BlockUtils {

    private val world get() = MinecraftCompat.localWorldOrThrow

    fun SkyHanniVector.getBlockAt(): Block = getBlockStateAt().block

    fun SkyHanniVector.getBlockStateAt(): BlockState = world.getBlockState(toBlockPos())

    fun SkyHanniVector.isInLoadedChunk(): Boolean =
        world.chunkSource.hasChunk(x.toInt() shr 4, z.toInt() shr 4)

    fun getTextureFromSkull(position: SkyHanniVector): String? {
        val entity = world.getBlockEntity(position.toBlockPos()) as? SkullBlockEntity ?: return null
        return entity.getSkullTexture()
    }

    fun SkullBlockEntity.getSkullTexture(): String? {
        return this.ownerProfile?.partialProfile()?.id?.toString()
    }

    fun BlockState.isBabyCrop(): Boolean {
        val property = (block.stateDefinition.properties.find { it.name == "age" } as? IntegerProperty) ?: return false
        return getValue(property) == 0
    }

    private fun raycast(start: SkyHanniVector, direction: SkyHanniVector, distance: Double = 50.0): SkyHanniVector {
        val target = start + direction.normalized() * distance
        val result = raycast(start, target)

        return result.location.toSkyHanniVector()
    }

    fun raycast(start: SkyHanniVector, end: SkyHanniVector): BlockHitResult = world.clip(
        ClipContext(
            start.toVec3(),
            end.toVec3(),
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            MinecraftCompat.localPlayerOrThrow,
        ),
    )

    fun getTargetedBlock(): SkyHanniVector? =
        Minecraft.getInstance().hitResult?.takeIf { it.type == HitResult.Type.BLOCK }
            ?.location?.toSkyHanniVector()?.floor()

    fun getTargetedBlockAtDistance(distance: Double) = raycast(
        LocationUtils.playerEyeLocation(),
        MinecraftCompat.localPlayerOrThrow.lookAngle.toSkyHanniVector(),
        distance,
    ).floor()

    private fun nearbyBlocks(center: SkyHanniVector, distance: Int): MutableIterable<BlockPos> {
        val offset = distance.toDouble()
        val from = center.add(-offset, -offset, -offset).toBlockPos()
        val to = center.add(offset, offset, offset).toBlockPos()
        return BlockPos.betweenClosed(from, to)
    }

    fun nearbyBlocks(
        center: SkyHanniVector,
        distance: Int,
        radius: Int = distance,
        condition: (BlockState) -> Boolean,
    ): Map<SkyHanniVector, BlockState> = nearbyBlocks(center, distance).mapNotNull {
        val loc = it.toSkyHanniVector()
        val state = loc.getBlockStateAt()
        if (condition(state) && center.distance(loc) <= radius) {
            loc to state
        } else null
    }.toMap()

    fun nearbyBlocks(
        center: SkyHanniVector,
        distance: Int,
        radius: Int = distance,
        filter: Block,
    ): Map<SkyHanniVector, BlockState> =
        nearbyBlocks(center, distance, radius, condition = { it.block == filter })

    val redstoneOreBlocks = buildList { addRedstoneOres() }
}
