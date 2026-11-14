package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

sealed class BitsUpdateEvent(val bits: Int, val bitsAvailable: Int, val difference: Int) : SkyHanniEvent() {
    @PrimaryFunction("onBitsGain")
    class BitsGain(bits: Int, bitsAvailable: Int, difference: Int) : BitsUpdateEvent(bits, bitsAvailable, difference)
    @PrimaryFunction("onBitsSpent")
    class BitsSpent(bits: Int, bitsAvailable: Int, difference: Int) : BitsUpdateEvent(bits, bitsAvailable, difference)
}

@PrimaryFunction("onBitsAvailableUpdate")
class BitsAvailableUpdateEvent(val bitsAvailable: Int) : SkyHanniEvent()
