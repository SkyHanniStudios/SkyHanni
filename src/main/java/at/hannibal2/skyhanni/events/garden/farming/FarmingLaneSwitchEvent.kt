package at.hannibal2.skyhanni.events.garden.farming

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.garden.farming.lane.FarmingLane
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

@PrimaryFunction("onFarmingLaneSwitch")
class FarmingLaneSwitchEvent(val lane: FarmingLane?) : SkyHanniEvent()
