package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.GetFromSackApi
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.SackApi
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.ItemUtils

@SkyHanniModule
object SuperCraftFeatures {

    private val config get() = SkyHanniMod.feature.inventory.gfs

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent.Allow) {
        if (!config.superCraftGFS) return
        val craftedItem = ItemUtils.readSuperCraftedItem(event.message) ?: return
        if (!SackApi.sackListInternalNames.contains(craftedItem.internalName.asString())) return
        DelayedRun.runNextTick {
            GetFromSackApi.getFromChatMessageSackItems(craftedItem)
        }
    }
}
