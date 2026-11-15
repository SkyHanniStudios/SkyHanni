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
    private val categories = mutableMapOf<String, KeyMapping.Category>()
    private val syncMutex = Mutex()

    fun init(modules: List<Any>) {
        val loaded = modules.mapTo(mutableSetOf()) { it.javaClass }
        LoadedKeyBinds.registrations.filter { it.owner in loaded }.forEach(::register)
    }

    fun visibleOnly(keyMappings: Array<KeyMapping>): Array<KeyMapping> =
        keyMappings.filterNot { it is SkyHanniKeyMapping && !it.keyBind.condition() }.toTypedArray()

    private fun register(registration: KeyBindRegistration) {
        val category = categories.getOrPut(registration.categoryPath) {
            KeyMapping.Category.register(SkyHanniMod.id(registration.categoryPath))
        }
        val mapping = SkyHanniKeyMapping(registration, registration.keyBind(), category)
        KeyMappingHelper.registerKeyMapping(mapping)
        mappings += mapping
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
    private fun onKeyDown(event: KeyDownEvent) = dispatch(event.keyCode, hold = false)

    @HandleEvent
    private fun onKeyPress(event: KeyPressEvent) = dispatch(event.keyCode, hold = true)

    private fun dispatch(keyCode: Int, hold: Boolean) = mappings.forEach { mapping ->
        if (mapping.registration.hold == hold && mapping.boundKeyCode == keyCode && mapping.isActive()) mapping.keyBind.onTrigger()
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
