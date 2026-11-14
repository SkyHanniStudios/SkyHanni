package at.hannibal2.skyhanni.events.experiments

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

/**
 * Fired when an Ultra Rare reward is uncovered in the Superpairs Experimentation Table game.
 *
 * @param dropName The display name of the uncovered item.
 * @param isBook Whether the drop is an enchanted book. False for non-book Ultra Rare items,
 *               which are detected at card-flip time via internal-name matching against the repo list.
 */
@PrimaryFunction("onTableRareUncover")
class TableRareUncoverEvent(val dropName: String, val isBook: Boolean = true) : SkyHanniEvent()
