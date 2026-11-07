package at.hannibal2.skyhanni.compat

import at.hannibal2.skyhanni.utils.SafeItemStack

//? if rei_compat {
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import at.hannibal2.skyhanni.utils.compat.MouseCompat
import me.shedaniel.math.Point
import me.shedaniel.rei.api.client.REIRuntime
import me.shedaniel.rei.api.client.gui.widgets.Slot
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
import me.shedaniel.rei.api.common.plugins.PluginManager
import net.minecraft.client.gui.components.events.ContainerEventHandler
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
//?}

object ReiCompat {
    @JvmStatic
    fun searchHasFocus(): Boolean {
        //? if rei_compat {
        if (!isReiReady()) return false
        if (MinecraftCompat.screen == null) return false
        return try {
            (REIRuntime.getInstance().searchTextField as? GuiEventListener)?.isFocused == true
        } catch (e: Throwable) {
            false
        }
        //?} else
        //return false
    }

    fun getHoveredStackFromRei(): SafeItemStack? {
        //? if rei_compat {
        if (!isReiReady()) return null
        return try {
            getItemStackFromItemList() ?: (MinecraftCompat.screen as? AbstractContainerScreen<*>)
                ?.let(::getItemStackFromRecipe)
        } catch (e: Throwable) {
            null
        }
        //?} else
        //return null
    }

//? if rei_compat {
    private val isReiLoaded by lazy { PlatformUtils.isModInstalled("roughlyenoughitems") }

    private fun isReiReady(): Boolean {
        if (!isReiLoaded) return false
        return try {
            !PluginManager.areAnyReloading() &&
                EntryTypeRegistry.getInstance().get(VanillaEntryTypes.ITEM.id) != null
        } catch (e: Throwable) {
            false
        }
    }

    private fun getItemStackFromRecipe(screen: AbstractContainerScreen<*>): SafeItemStack? {
        val entryStack = ScreenRegistry.getInstance().getFocusedStack(screen, currentMousePoint())
            ?: return null
        return entryStack.value as? SafeItemStack ?: entryStack.cheatsAs().value
    }

    private fun getItemStackFromItemList(): SafeItemStack? {
        var baseElement: GuiEventListener? = REIRuntime.getInstance().overlay.orElse(null)
        val mousePoint = currentMousePoint()
        while (true) {
            if (baseElement is Slot) return baseElement.currentEntry.cheatsAs().value
            if (baseElement !is ContainerEventHandler) return null
            baseElement = baseElement.getChildAt(mousePoint.x.toDouble(), mousePoint.y.toDouble()).orElse(null)
        }
    }

    private fun currentMousePoint(): Point = Point(MouseCompat.getX(), MouseCompat.getY())
    //?}
}
