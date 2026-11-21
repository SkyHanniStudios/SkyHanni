package at.hannibal2.skyhanni.events.inventory

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.utils.SafeItemStack

@PrimaryFunction("onAnvilUpdate")
class AnvilUpdateEvent(val left: SafeItemStack?, val right: SafeItemStack?) : SkyHanniEvent()
