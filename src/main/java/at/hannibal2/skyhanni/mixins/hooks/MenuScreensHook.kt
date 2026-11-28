package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.features.dungeon.DungeonSpiritLeapOverlay
import at.hannibal2.skyhanni.features.dungeon.DungeonSpiritLeapScreen
import at.hannibal2.skyhanni.features.inventory.wardrobe.CustomWardrobe
import at.hannibal2.skyhanni.features.inventory.wardrobe.CustomWardrobeEditScreen
import at.hannibal2.skyhanni.features.inventory.wardrobe.CustomWardrobeScreen
import at.hannibal2.skyhanni.features.mining.BlockStrengthGuide
import at.hannibal2.skyhanni.features.mining.BlockStrengthGuideScreen
import at.hannibal2.skyhanni.utils.SkyBlockUtils
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import at.hannibal2.skyhanni.utils.compat.unformattedTextCompat
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.MenuType

// Reference: https://github.com/SkyblockerMod/Skyblocker/blob/main/src/main/java/de/hysky/skyblocker/mixins/MenuScreensConstructorMixin.java
object MenuScreensHook {
    @JvmStatic
    fun <T : AbstractContainerMenu> openCustomMenu(
        name: Component,
        type: MenuType<T>,
        client: Minecraft,
        id: Int,
    ): Boolean {
        if (!SkyBlockUtils.inSkyBlock) return false

        val player = client.player ?: return false
        val inventory = player.inventory
        val inventoryName = name.unformattedTextCompat()

        if (openCustomWardrobe(inventoryName, name, type, client, id, inventory)) {
            return true
        }

        if (openBlockStrengthGuide(inventoryName, name, type, client, id, inventory)) {
            return true
        }

        return openSpiritLeap(inventoryName, name, type, client, id, inventory)
    }

    private fun <T : AbstractContainerMenu> openSpiritLeap(
        inventoryName: String,
        name: Component,
        type: MenuType<T>,
        client: Minecraft,
        id: Int,
        inventory: Inventory,
    ): Boolean {
        if (!DungeonSpiritLeapOverlay.shouldReplace(inventoryName)) return false

        val menu = type.create(id, inventory) as? ChestMenu ?: return false

        client.player?.containerMenu = menu
        MinecraftCompat.screen = DungeonSpiritLeapScreen(menu, name)

        return true
    }

    private fun <T : AbstractContainerMenu> openBlockStrengthGuide(
        inventoryName: String,
        name: Component,
        type: MenuType<T>,
        client: Minecraft,
        id: Int,
        inventory: Inventory,
    ): Boolean {
        if (!BlockStrengthGuide.shouldReplace(inventoryName)) return false

        val menu = type.create(id, inventory) as? ChestMenu ?: return false

        client.player?.containerMenu = menu
        MinecraftCompat.screen = BlockStrengthGuideScreen(menu, name)

        return true
    }

    private fun <T : AbstractContainerMenu> openCustomWardrobe(
        inventoryName: String,
        name: Component,
        type: MenuType<T>,
        client: Minecraft,
        id: Int,
        inventory: Inventory,
    ): Boolean {
        if (!CustomWardrobe.shouldReplace(inventoryName)) return false

        val menu = type.create(id, inventory) as? ChestMenu ?: return false

        client.player?.containerMenu = menu

        when (val screen = MinecraftCompat.screen) {
            is CustomWardrobeScreen -> screen.changeHandler(menu, name)
            is CustomWardrobeEditScreen ->
                MinecraftCompat.screen = CustomWardrobeEditScreen(menu, inventory, name)
            else ->
                MinecraftCompat.screen = CustomWardrobeScreen(menu, name)
        }

        return true
    }
}
