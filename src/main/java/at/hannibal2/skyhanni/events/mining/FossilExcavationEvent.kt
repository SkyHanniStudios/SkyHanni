package at.hannibal2.skyhanni.events.mining

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

@PrimaryFunction("onFossilExcavation")
class FossilExcavationEvent(val loot: List<Pair<String, Int>>) : SkyHanniEvent()
