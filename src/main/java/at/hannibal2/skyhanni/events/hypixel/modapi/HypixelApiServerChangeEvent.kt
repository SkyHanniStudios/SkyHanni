package at.hannibal2.skyhanni.events.hypixel.modapi

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import net.hypixel.data.type.ServerType

@PrimaryFunction("onServerChange")
data class HypixelApiServerChangeEvent(
    val serverName: String?,
    val serverType: ServerType?,
    val lobbyName: String?,
    val mode: String?,
    val map: String?,
) : SkyHanniEvent()
