package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.ArrowType
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

@PrimaryFunction("onQuiverUpdate")
class QuiverUpdateEvent(val currentArrow: ArrowType?, val currentAmount: Int) : SkyHanniEvent()
