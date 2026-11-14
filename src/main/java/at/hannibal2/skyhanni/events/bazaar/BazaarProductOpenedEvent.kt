package at.hannibal2.skyhanni.events.bazaar

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.utils.NeuInternalName

@PrimaryFunction("onBazaarProductOpened")
class BazaarProductOpenedEvent(val openedProduct: NeuInternalName?, val inventoryOpenEvent: InventoryFullyOpenedEvent) : SkyHanniEvent()
