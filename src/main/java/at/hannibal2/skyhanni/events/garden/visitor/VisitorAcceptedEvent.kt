package at.hannibal2.skyhanni.events.garden.visitor

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.garden.visitor.VisitorApi
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

// TODO disambiguate with VisitorAcceptEvent
@PrimaryFunction("onVisitorAccepted")
class VisitorAcceptedEvent(val visitor: VisitorApi.Visitor) : SkyHanniEvent()
