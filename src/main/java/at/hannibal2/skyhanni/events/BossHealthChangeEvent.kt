package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorEntityData
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

// TODO Move to MobEvent.kt
@PrimaryFunction("onBossHealthChange")
class BossHealthChangeEvent(
    val entityData: DamageIndicatorEntityData,
    val lastHealth: Long,
    val health: Long,
    val maxHealth: Long,
) : SkyHanniEvent()
