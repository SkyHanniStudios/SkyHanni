package at.hannibal2.skyhanni.features.inventory.wardrobe

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.utils.ColorUtils.darker
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.compat.SkyHanniContainerScreen
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ChestMenu
import java.awt.Color

class CustomWardrobeEditScreen(
    menu: ChestMenu,
    inventory: Inventory,
    title: Component,
) : SkyHanniContainerScreen(menu, inventory, title) {
    private val inventoryButtonPosition: Position = Position().ignoreScale()
    private var inventoryButton: Renderable? = null

    private val enableButtonColor = Color(116, 150, 255, 200)

    override fun init() = super.init().also {
        CustomWardrobe.switchingScreens = false
    }

    override fun removed() {
        if (!CustomWardrobe.switchingScreens) {
            super.removed()
        }
    }

    override fun onDrawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val renderable = inventoryButton ?: getReEnableButton().also { inventoryButton = it }
        val posX = this.leftPos + (1.05 * this.imageWidth).toInt()
        val posY = this.topPos + (this.imageHeight - renderable.height) / 2
        inventoryButtonPosition.moveTo(posX, posY).renderRenderable(
            renderable,
            posLabel = CustomWardrobe.GUI_NAME,
            addToGuiManager = false,
        )
    }

    private fun getReEnableButton(): Renderable = CustomWardrobe.createLabeledButton(
        "§bEdit",
        hoveredColor = enableButtonColor,
        unhoveredColor = enableButtonColor.darker(0.8),
        onClick = {
            CustomWardrobe.exitEditMode()
        },
    )
}
