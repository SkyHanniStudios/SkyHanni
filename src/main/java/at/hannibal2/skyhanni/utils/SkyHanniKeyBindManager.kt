package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils.afterChange
import at.hannibal2.skyhanni.utils.KeyboardManager.toInputKey
import at.hannibal2.skyhanni.utils.StringUtils.toIdentifierPath
import at.hannibal2.skyhanni.utils.coroutines.MutexUtils.tryWithLock
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import kotlinx.coroutines.sync.Mutex

@SkyHanniModule
object SkyHanniKeyBindManager {

    private const val MAX_PER_CATEGORY = 20

    private val keybinds = mutableListOf<SkyHanniKeyBind>()
    private val categoryCounts = mutableMapOf<String, Int>()
    private val categories = mutableMapOf<String, KeyMapping.Category>()
    private val syncMutex = Mutex()

    fun register(keybind: SkyHanniKeyBind) {
        KeyMappingHelper.registerKeyMapping(keybind)
        keybinds += keybind
    }

    fun mappingName(category: String, displayName: String): String =
        "skyhanni.${category.toIdentifierPath()}.${displayName.toIdentifierPath()}"

    fun categoryFor(category: String): KeyMapping.Category {
        val index = categoryCounts.getOrDefault(category, 0).also { categoryCounts[category] = it + 1 }
        val page = index / MAX_PER_CATEGORY
        val label = if (page == 0) "SkyHanni - $category" else "SkyHanni - $category ${page + 1}"
        return categories.getOrPut(label) { KeyMapping.Category.register(SkyHanniMod.id(label.toIdentifierPath())) }
    }

    fun visibleOnly(keyMappings: Array<KeyMapping>): Array<KeyMapping> =
        keyMappings.filterNot { it is SkyHanniKeyBind && !it.condition() }.toTypedArray()

    @HandleEvent
    private fun onInitFinished() = keybinds.forEach { keybind ->
        val property = keybind.keyProvider()
        keybind.applyConfigKey(property.get())
        property.afterChange {
            if (keybind.applyConfigKey(this)) Minecraft.getInstance().options.save()
        }
    }

    internal fun SkyHanniKeyBind.pushKeyToConfig(key: InputConstants.Key) {
        val property = keyProvider()
        if (property.get() != key.value) syncMutex.tryWithLock { property.set(key.value) }
    }

    private fun SkyHanniKeyBind.applyConfigKey(keyCode: Int): Boolean {
        if (key.value == keyCode) return false
        return syncMutex.tryWithLock {
            setKey(keyCode.toInputKey())
            KeyMapping.resetMapping()
        } != null
    }
}
