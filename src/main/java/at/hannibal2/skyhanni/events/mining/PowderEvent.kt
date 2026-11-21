package at.hannibal2.skyhanni.events.mining

import at.hannibal2.skyhanni.api.HotmApi
import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

sealed class PowderEvent(val powder: HotmApi.PowderType) : SkyHanniEvent() {
    @PrimaryFunction("onPowderGain")
    class Gain(powder: HotmApi.PowderType, val amount: Long) : PowderEvent(powder)
    @PrimaryFunction("onPowderSpent")
    class Spent(powder: HotmApi.PowderType, val amount: Long) : PowderEvent(powder)
    @PrimaryFunction("onPowderReset")
    class Reset(powder: HotmApi.PowderType) : PowderEvent(powder)
}
