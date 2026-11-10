package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

@PrimaryFunction("onTitleReceived")
class TitleReceivedEvent(
    val title: String,
    val isSubtitle: Boolean,
) : CancellableSkyHanniEvent()
