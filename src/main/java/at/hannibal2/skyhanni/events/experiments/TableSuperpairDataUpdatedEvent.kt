package at.hannibal2.skyhanni.events.experiments

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

/**
 * Fired when the tracked Superpairs pair data changes after a processed card click.
 */
@PrimaryFunction("onSuperpairDataUpdated")
class TableSuperpairDataUpdatedEvent : SkyHanniEvent()
