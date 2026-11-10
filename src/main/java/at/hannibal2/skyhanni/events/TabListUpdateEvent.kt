package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import net.minecraft.network.chat.Component

@PrimaryFunction("onTabListUpdate")
class TabListUpdateEvent(val tabList: List<Component>) : SkyHanniEvent()
