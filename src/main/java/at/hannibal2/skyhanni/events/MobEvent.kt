package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import net.minecraft.world.damagesource.DamageSource

sealed class MobEvent(val mob: Mob) : SkyHanniEvent() {
    sealed class Spawn(mob: Mob) : MobEvent(mob) {
        @PrimaryFunction("onSkyblockMobSpawn")
        class SkyblockMob(mob: Mob) : Spawn(mob)
        @PrimaryFunction("onSummonSpawn")
        class Summon(mob: Mob) : Spawn(mob)
        @PrimaryFunction("onPlayerSpawn")
        class Player(mob: Mob) : Spawn(mob)
        @PrimaryFunction("onDisplayNpcSpawn")
        class DisplayNpc(mob: Mob) : Spawn(mob)
        @PrimaryFunction("onSpecialSpawn")
        class Special(mob: Mob) : Spawn(mob)
        @PrimaryFunction("onProjectileSpawn")
        class Projectile(mob: Mob) : Spawn(mob)
    }

    sealed class DeSpawn(mob: Mob) : MobEvent(mob) {
        @PrimaryFunction("onSkyblockMobDeSpawn")
        class SkyblockMob(mob: Mob) : DeSpawn(mob)
        @PrimaryFunction("onSummonDeSpawn")
        class Summon(mob: Mob) : DeSpawn(mob)
        @PrimaryFunction("onPlayerDeSpawn")
        class Player(mob: Mob) : DeSpawn(mob)
        @PrimaryFunction("onDisplayNpcDeSpawn")
        class DisplayNpc(mob: Mob) : DeSpawn(mob)
        @PrimaryFunction("onSpecialDeSpawn")
        class Special(mob: Mob) : DeSpawn(mob)
        @PrimaryFunction("onProjectileDeSpawn")
        class Projectile(mob: Mob) : DeSpawn(mob)
    }

    sealed class FirstSeen(mob: Mob) : MobEvent(mob) {
        @PrimaryFunction("onSkyblockMobFirstSeen")
        class SkyblockMob(mob: Mob) : FirstSeen(mob)
        @PrimaryFunction("onSummonFirstSeen")
        class Summon(mob: Mob) : FirstSeen(mob)
        @PrimaryFunction("onPlayerFirstSeen")
        class Player(mob: Mob) : FirstSeen(mob)
        @PrimaryFunction("onDisplayNpcFirstSeen")
        class DisplayNpc(mob: Mob) : FirstSeen(mob)
        @PrimaryFunction("onSpecialFirstSeen")
        class Special(mob: Mob) : FirstSeen(mob)
        @PrimaryFunction("onProjectileFirstSeen")
        class Projectile(mob: Mob) : FirstSeen(mob)
    }

    sealed class Hurt(mob: Mob, val source: DamageSource, val amount: Float) : MobEvent(mob) {
        @PrimaryFunction("onSkyblockMobHurt")
        class SkyblockMob(mob: Mob, source: DamageSource, amount: Float) : Hurt(mob, source, amount)
        @PrimaryFunction("onSummonHurt")
        class Summon(mob: Mob, source: DamageSource, amount: Float) : Hurt(mob, source, amount)
        @PrimaryFunction("onPlayerHurt")
        class Player(mob: Mob, source: DamageSource, amount: Float) : Hurt(mob, source, amount)
        @PrimaryFunction("onDisplayNpcHurt")
        class DisplayNpc(mob: Mob, source: DamageSource, amount: Float) : Hurt(mob, source, amount)
        @PrimaryFunction("onSpecialHurt")
        class Special(mob: Mob, source: DamageSource, amount: Float) : Hurt(mob, source, amount)
        @PrimaryFunction("onProjectileHurt")
        class Projectile(mob: Mob, source: DamageSource, amount: Float) : Hurt(mob, source, amount)
    }
}
