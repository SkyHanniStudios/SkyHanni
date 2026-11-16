package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.KeyDownEvent
import at.hannibal2.skyhanni.events.minecraft.KeyPressEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils.afterChange
import at.hannibal2.skyhanni.utils.KeyboardManager.toInputKey
import at.hannibal2.skyhanni.utils.coroutines.MutexUtils.tryWithLock
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import kotlinx.coroutines.sync.Mutex

@SkyHanniModule
object SkyHanniKeyBindManager {

    private val mappings = mutableListOf<SkyHanniKeyMapping>()
    private val categoryCounts = mutableMapOf<KeyBindCategory, Int>()
    private val categories = mutableMapOf<String, KeyMapping.Category>()
    private val syncMutex = Mutex()

    @HandleEvent
    private fun onPreInitFinished() = LoadedKeyBinds.keyBinds.forEach { (options, keyBind) ->
        val mapping = SkyHanniKeyMapping(options, keyBind, categoryFor(options.category))
        KeyMappingHelper.registerKeyMapping(mapping)
        mappings += mapping
    }

    fun visibleOnly(keyMappings: Array<KeyMapping>): Array<KeyMapping> =
        keyMappings.filterNot { it is SkyHanniKeyMapping && !it.keyBind.condition() }.toTypedArray()

    private fun categoryFor(category: KeyBindCategory): KeyMapping.Category {
        val index = categoryCounts.getOrDefault(category, 0).also { categoryCounts[category] = it + 1 }
        val path = KeyBindIds.categoryPath(category, index / KeyBindIds.PAGE_SIZE)
        return categories.getOrPut(path) { KeyMapping.Category.register(SkyHanniMod.id(path)) }
    }

    @HandleEvent
    private fun onInitFinished() = mappings.forEach { mapping ->
        val property = mapping.keyBind.property
        mapping.applyConfigKey(property.get())
        property.afterChange {
            if (mapping.applyConfigKey(this)) Minecraft.getInstance().options.save()
        }
    }

    @HandleEvent
    private fun onKeyDown(event: KeyDownEvent) = dispatch(event.keyCode, KeyBindTrigger.PRESS)

    @HandleEvent
    private fun onKeyPress(event: KeyPressEvent) = dispatch(event.keyCode, KeyBindTrigger.HOLD)

    private fun dispatch(keyCode: Int, trigger: KeyBindTrigger) = mappings.forEach { mapping ->
        if (mapping.options.trigger == trigger && mapping.boundKeyCode == keyCode && mapping.isActive()) mapping.keyBind.onTrigger()
    }

    internal fun SkyHanniKeyMapping.pushKeyToConfig(key: InputConstants.Key) {
        val property = keyBind.property
        if (property.get() != key.value) syncMutex.tryWithLock { property.set(key.value) }
    }

    private fun SkyHanniKeyMapping.applyConfigKey(keyCode: Int): Boolean {
        if (boundKeyCode == keyCode) return false
        return syncMutex.tryWithLock {
            setKey(keyCode.toInputKey())
            KeyMapping.resetMapping()
        } != null
    }
}
