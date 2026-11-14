package at.hannibal2.skyhanni.utils.compat

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ChestMenu

abstract class SkyHanniContainerScreen(
    menu: ChestMenu,
    inventory: Inventory,
    title: Component,
) : ContainerScreen(menu, inventory, title), SkyHanniScreenHooks {

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
        drawGuarded(graphics, mouseX, mouseY, partialTick)
    }
}
