package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

// TODO migrate to a MobEvent type
@PrimaryFunction("onDamageIndicatorFinalBoss")
class DamageIndicatorFinalBossEvent(val id: Int) : SkyHanniEvent()
