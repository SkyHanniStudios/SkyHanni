package at.hannibal2.skyhanni.events.diana

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.utils.LorenzVec

// TODO merge with other Burrow events into `BurrowEvents.kt`
@PrimaryFunction("onBurrowDug")
class BurrowDugEvent(val burrowLocation: LorenzVec, val current: Int, val max: Int) : SkyHanniEvent()
