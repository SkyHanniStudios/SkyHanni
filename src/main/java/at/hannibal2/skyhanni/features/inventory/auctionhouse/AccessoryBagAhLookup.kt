package at.hannibal2.skyhanni.features.inventory.auctionhouse

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.BitsApi
import at.hannibal2.skyhanni.events.AccessoryBagUpdateEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.editItemInfo
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLoreComponent
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.SafeItemStack
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockUtils
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ChestMenu
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object AccessoryBagAhLookup {

    private val config get() = SkyHanniMod.feature.inventory.auctions
    private fun isEnabled() = SkyBlockUtils.inSkyBlock && config.accessoryBagAhLookup

    private const val GREY_OPACITY = 175
    private var lastClick = SimpleTimeMark.farPast()

    private data class AccessoryItem(
        val cleanName: String,
        val displayStack: SafeItemStack,
    )

    private val accessoryItems = mutableMapOf<Int, AccessoryItem>()

    @HandleEvent(onlyOnSkyblock = true)
    private fun onAccessoryBagUpdate(event: AccessoryBagUpdateEvent) {
        if (!isEnabled()) return
        accessoryItems.clear()

        for ((slot, stack) in event.inventoryItems) {
            if (stack.getInternalNameOrNull() != null) continue

            val name = stack.cleanName
            if (name.isBlank()) continue

            val resolved = NeuInternalName.fromItemNameOrNull(name) ?: continue
            val displayStack = resolved.getItemStack().copy()
                .editItemInfo(stack.hoverName, stack.getLoreComponent())

            accessoryItems[slot] = AccessoryItem(
                cleanName = name,
                displayStack = displayStack,
            )
        }
    }

    @HandleEvent(InventoryCloseEvent::class)
    private fun onInventoryClose() {
        accessoryItems.clear()
    }

    @HandleEvent(onlyOnSkyblock = true)
    private fun onForegroundDrawn(event: GuiContainerEvent.ForegroundDrawnEvent) {
        if (!isEnabled()) return
        if (accessoryItems.isEmpty()) return
        if (event.gui !is ContainerScreen) return

        val chest = event.container as? ChestMenu ?: return

        for ((slot, _) in chest.getUpperItems()) {
            if (slot.index in accessoryItems) {
                slot.highlight(LorenzColor.DARK_GRAY.addOpacity(GREY_OPACITY))
            }
        }
    }

    @HandleEvent(onlyOnSkyblock = true, priority = HandleEvent.HIGH)
    private fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!isEnabled()) return
        val accessoryItem = accessoryItems[event.slotId] ?: return
        if (!event.mouseType.isRightClick()) return

        event.cancel()

        if (lastClick.passedSince() <= 0.3.seconds) return
        lastClick = SimpleTimeMark.now()

        val cookieTime = BitsApi.cookieBuffTime
        if (cookieTime != null && !cookieTime.isInFuture()) {
            ChatUtils.userError("You need to have an active Booster Cookie to use this feature!")
            return
        }

        HypixelCommands.auctionSearch(accessoryItem.cleanName)
    }

    @HandleEvent
    private fun replaceItem(event: ReplaceItemEvent) {
        if (!isEnabled()) return
        if (event.inventory is Inventory) return
        val accessoryItem = accessoryItems[event.slot] ?: return
        event.replace(accessoryItem.displayStack)
    }
}
