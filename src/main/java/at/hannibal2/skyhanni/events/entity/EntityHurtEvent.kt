package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.GenericSkyHanniEvent
import at.hannibal2.skyhanni.events.entity.abstract.SkyHanniEntityEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity

@PrimaryFunction("onEntityHurt")
class EntityHurtEvent<T : Entity>(
    override val entity: T,
    val source: DamageSource,
    val amount: Float,
) : GenericSkyHanniEvent<T>(entity.javaClass), SkyHanniEntityEvent<T>
