package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.utils.SafeItemStack

@PrimaryFunction("onOwnInventoryArmorUpdate")
data class OwnInventoryArmorUpdateEvent(val itemStack: SafeItemStack, val slot: Int) : SkyHanniEvent()

@PrimaryFunction("onOwnInventoryItemUpdate")
data class OwnInventoryItemUpdateEvent(val itemStack: SafeItemStack, val slot: Int) : SkyHanniEvent()

@PrimaryFunction("onOwnInventoryMenuUpdate")
data class OwnInventoryMenuUpdateEvent(val itemStack: SafeItemStack) : SkyHanniEvent()
