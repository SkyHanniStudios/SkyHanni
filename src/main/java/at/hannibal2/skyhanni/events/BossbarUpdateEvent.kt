package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

@PrimaryFunction("onBossbarUpdate")
class BossbarUpdateEvent(val bossbar: String) : SkyHanniEvent()
