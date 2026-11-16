package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.highlight

@SkyHanniModule
object HighlightOnHoverSlot {
    val currentSlots = mutableMapOf<Pair<Int, Int>, List<Int>>()

    @HandleEvent
    private fun onInventoryOpen(event: InventoryOpenEvent) {
        currentSlots.clear()
    }

    @HandleEvent
    private fun onInventoryClose(event: InventoryCloseEvent) {
        currentSlots.clear()
    }

    @HandleEvent(priority = HandleEvent.LOW, onlyOnSkyblock = true)
    private fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        val list = currentSlots.flatMapTo(mutableSetOf()) { it.value }
        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.index in list) {
                slot.highlight(LorenzColor.GREEN)
            }
        }
    }
}
