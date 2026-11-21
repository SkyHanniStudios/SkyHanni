package at.hannibal2.skyhanni.events.diana

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.event.diana.GuessEntry
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

// TODO merge with other Burrow events into `BurrowEvents.kt`
@PrimaryFunction("onBurrowGuess")
class BurrowGuessEvent(val guess: GuessEntry, val source: String) : SkyHanniEvent()
