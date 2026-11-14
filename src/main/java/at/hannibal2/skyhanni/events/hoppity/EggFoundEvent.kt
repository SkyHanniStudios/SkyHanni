package at.hannibal2.skyhanni.events.hoppity

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

@PrimaryFunction("onEggFound")
class EggFoundEvent(
    val type: HoppityEggType,
    val slotIndex: Int? = null,
    val note: String? = null,
    val chatEvent: SkyHanniChatEvent.Allow? = null,
) : SkyHanniEvent()
