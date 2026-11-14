package at.hannibal2.skyhanni.events.minecraft

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

/** Counterpart to [KeyDownEvent]*/
@PrimaryFunction("onKeyUp")
class KeyUpEvent(val keyCode: Int) : SkyHanniEvent()
