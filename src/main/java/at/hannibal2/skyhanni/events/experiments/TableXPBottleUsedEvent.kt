package at.hannibal2.skyhanni.events.experiments

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.utils.NeuInternalName

@PrimaryFunction("onTableXpBottleUsed")
class TableXPBottleUsedEvent(val internalName: NeuInternalName, val amount: Int) : SkyHanniEvent()
