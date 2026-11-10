package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorEntityData
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import net.minecraft.world.entity.LivingEntity

// TODO migrate to a MobEvent type
@PrimaryFunction("onDamageIndicatorDeath")
class DamageIndicatorDeathEvent(val entity: LivingEntity, val data: DamageIndicatorEntityData) : SkyHanniEvent()

