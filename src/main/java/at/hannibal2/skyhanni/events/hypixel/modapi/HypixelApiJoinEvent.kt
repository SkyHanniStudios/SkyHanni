package at.hannibal2.skyhanni.events.hypixel.modapi

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

@PrimaryFunction("onHypixelJoin")
data class HypixelApiJoinEvent(val alpha: Boolean) : SkyHanniEvent()
