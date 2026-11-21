package at.hannibal2.skyhanni.events.player

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.utils.PlayerUtils

// TODO migrate to MobEvent subtype?
//  Would be interesting to incorporate the Modify aspect of this, as as parameter for those events.
abstract class AbstractPlayerDeathEvent(
    val name: String,
    val reason: String,
) : SkyHanniEvent() {
    val isSelf: Boolean = name == PlayerUtils.getName()
}

object PlayerDeathEvent {
    @PrimaryFunction("onPlayerDeath")
    class Allow(
        name: String,
        reason: String,
        val chatEvent: SkyHanniChatEvent.Allow,
    ) : AbstractPlayerDeathEvent(name, reason)

    @PrimaryFunction("onPlayerDeathModify")
    class Modify(
        name: String,
        reason: String,
        val chatEvent: SkyHanniChatEvent.Modify,
    ) : AbstractPlayerDeathEvent(name, reason)
}
