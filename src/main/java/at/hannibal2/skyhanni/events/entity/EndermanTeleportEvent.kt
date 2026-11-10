package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

// TODO move to a MobEvent target, or a generic EntityTeleportEvent
@PrimaryFunction("onEndermanTeleport")
class EndermanTeleportEvent : CancellableSkyHanniEvent()
