package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat

@SkyHanniModule
object ScreenData {
    private var wasOpen = false

    @HandleEvent
    private fun onTick() {
        val isOpen = MinecraftCompat.screen != null
        if (wasOpen == isOpen) return
        wasOpen = isOpen
        if (!wasOpen) {
            InventoryCloseEvent("?", false).post()
        }
    }
}
