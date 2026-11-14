package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

@PrimaryFunction("onTabListLineRender")
class TabListLineRenderEvent(var text: String) : SkyHanniEvent()
