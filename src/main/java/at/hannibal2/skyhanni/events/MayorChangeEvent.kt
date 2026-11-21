package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.ElectionCandidate
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

@PrimaryFunction("onMayorChange")
class MayorChangeEvent(
    val old: ElectionCandidate?,
    val new: ElectionCandidate?,
    val debug: Boolean = false,
) : SkyHanniEvent()
