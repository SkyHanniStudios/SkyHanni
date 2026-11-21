package at.hannibal2.skyhanni.events.mining

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.MiningEventsApi
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

sealed class MiningEventEvent(val event: MiningEventsApi.MiningEvent) : SkyHanniEvent() {
    @PrimaryFunction("onMiningEventStart")
    class Started(event: MiningEventsApi.MiningEvent) : MiningEventEvent(event)
    @PrimaryFunction("onMiningEventEnd")
    class Ended(event: MiningEventsApi.MiningEvent) : MiningEventEvent(event)
}

