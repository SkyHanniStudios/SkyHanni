package at.hannibal2.skyhanni.events.garden.pests

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

@PrimaryFunction("onPestTrapDataUpdate")
class PestTrapDataEvent(
    val trapsPlaced: Int,
    val fullTraps: Set<Int>,
    val noBaitTraps: Set<Int>,
) : SkyHanniEvent()
