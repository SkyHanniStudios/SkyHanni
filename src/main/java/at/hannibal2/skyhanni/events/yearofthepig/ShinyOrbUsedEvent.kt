package at.hannibal2.skyhanni.events.yearofthepig

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

/**
 * Fired when the player uses a Shiny Orb on a Shiny Pig in the Hub during the Year of the Pig.
 * Posted about a second after the click, once the orb has been found next to the pig and is tracked in
 * `PigFeaturesApi.dataSetList`. Not fired if no orb is found near the pig.
 */
@PrimaryFunction("onShinyOrbUsed")
class ShinyOrbUsedEvent : SkyHanniEvent()
