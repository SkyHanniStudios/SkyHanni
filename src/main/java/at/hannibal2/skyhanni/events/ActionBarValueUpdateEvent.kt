package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.ActionBarStatsData
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

@PrimaryFunction("onActionBarValueUpdate")
class ActionBarValueUpdateEvent(val updated: ActionBarStatsData) : SkyHanniEvent()
