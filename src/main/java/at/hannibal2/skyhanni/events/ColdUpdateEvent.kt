package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

/**
 * Fired when the player's cold level changes in cold-affected areas (Glacite Tunnels and Mineshaft).
 * Cold resets to 0 when the player warms up at a campfire or dies.
 * @param cold The new absolute cold level. Ranges from 0 to 100. At 100 the player freezes to death.
 */
@PrimaryFunction("onColdUpdate")
class ColdUpdateEvent(val cold: Int) : SkyHanniEvent()
