package at.hannibal2.skyhanni.events.kuudra

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

// TODO migrate to a KuudraEvents.kt with Enter/Complete, and use enums instead of int
@PrimaryFunction("onKuudraEnter")
class KuudraEnterEvent(val kuudraTier: Int) : SkyHanniEvent()
