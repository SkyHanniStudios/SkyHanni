package at.hannibal2.skyhanni.events.diana

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.vector.SkyHanniVector

class BurrowDugEvent(val burrowLocation: SkyHanniVector, val current: Int, val max: Int) : SkyHanniEvent()
