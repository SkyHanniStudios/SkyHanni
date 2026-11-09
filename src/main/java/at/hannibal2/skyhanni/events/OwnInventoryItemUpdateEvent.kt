package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.utils.SafeItemStack

@PrimaryFunction("onInventoryArmorUpdate")
data class OwnInventoryArmorUpdateEvent(val itemStack: SafeItemStack, val slot: Int) : SkyHanniEvent()

@PrimaryFunction("onInventoryItemUpdate")
data class OwnInventoryItemUpdateEvent(val itemStack: SafeItemStack, val slot: Int) : SkyHanniEvent()

@PrimaryFunction("onInventoryMenuUpdate")
data class OwnInventoryMenuUpdateEvent(val itemStack: SafeItemStack) : SkyHanniEvent()
