package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ParticleEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils.getArmorInventory
import at.hannibal2.skyhanni.utils.EntityUtils.getEntitiesNearby
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getArmorDye
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball

@SkyHanniModule
object ParticleHider {

    private val config get() = SkyHanniMod.feature.misc.particleHiders

    private val smokeTypes = setOf(
        ParticleTypes.SMOKE,
        ParticleTypes.LARGE_SMOKE,
    )
    private const val NEARBY_PARTICLE_RADIUS = 5.0
    private const val FAR_PARTICLE_DISTANCE = 40
    private fun inM7Boss() = DungeonApi.inDungeon() && DungeonApi.dungeonFloor == "M7" && DungeonApi.inBossRoom

    @HandleEvent
    fun onParticle(event: ParticleEvent) {
        with(event) {
            val conditions = setOf(
                (config.hideFarParticles && distanceToPlayer > FAR_PARTICLE_DISTANCE && !inM7Boss()),
                (config.hideCloseRedstoneParticles && type == ParticleTypes.DUST && distanceToPlayer < 2),
                (config.hideFireballParticles && type in smokeTypes &&
                    event.location.getEntitiesNearby<SmallFireball>(NEARBY_PARTICLE_RADIUS).isNotEmpty()),
                (config.hideDyeParticles && !longDistance &&
                    shouldHideDyeParticles(MinecraftCompat.localPlayerOrNull, type)),
            )

            if (conditions.any { it }) event.cancel()
        }
    }

    @JvmStatic
    fun shouldHideBlockParticles(): Boolean {
        val config = config.blockBreakParticle
        return when {
            !config.hide -> false
            config.onlyInGarden -> IslandType.GARDEN.isInIsland()
            else -> true
        }
    }

    @JvmStatic
    fun shouldHideFireParticles() = MinecraftCompat.localWorldExists && config.hideFireBlockParticles

    @JvmStatic
    fun shouldHideBlazeParticles() = MinecraftCompat.localWorldExists && config.hideBlazeParticles

    @JvmStatic
    fun shouldHideFireballParticles() = MinecraftCompat.localWorldExists && config.hideFireballParticles

    fun shouldHideDyeParticles(player: LocalPlayer?, type: ParticleType<*>): Boolean {
        val hasCustomDyeOnArmor = player?.getArmorInventory()?.any {
            it?.getArmorDye()?.asString() in setOf("DYE_SLUDGE", "DYE_SPIRIT", "DYE_SPOOKY")
        } ?: false
        if (!hasCustomDyeOnArmor) return false

        return type in setOf(ParticleTypes.COPPER_FIRE_FLAME, ParticleTypes.BUBBLE, ParticleTypes.WITCH)
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "misc.hideBlazeParticles", "misc.particleHiders.hideBlazeParticles")
        event.move(3, "misc.hideEndermanParticles", "misc.particleHiders.hideEndermanParticles")
        event.move(3, "misc.hideFarParticles", "misc.particleHiders.hideFarParticles")
        event.move(3, "misc.hideFireballParticles", "misc.particleHiders.hideFireballParticles")
        event.move(3, "misc.hideCloseRedstoneparticles", "misc.particleHiders.hideCloseRedstoneParticles")
        event.move(3, "misc.hideFireBlockParticles", "misc.particleHiders.hideFireBlockParticles")
        event.move(3, "misc.hideSmokeParticles", "misc.particleHiders.hideSmokeParticles")
    }
}
