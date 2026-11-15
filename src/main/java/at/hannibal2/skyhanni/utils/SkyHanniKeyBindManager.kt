package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.KeyDownEvent
import at.hannibal2.skyhanni.events.minecraft.KeyPressEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
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
        val mapping = SkyHanniKeyMapping(registration, registration.readKeyBind(), category)
        KeyMappingHelper.registerKeyMapping(mapping)
        mappings += mapping
    }

    private fun KeyBindRegistration.readKeyBind(): SkyHanniKeyBind {
        val field = owner.getDeclaredField(fieldName).apply { trySetAccessible() }
        return field.get(null) as? SkyHanniKeyBind
            ?: ErrorManager.skyHanniError("Registered key bind ${owner.name}.$fieldName is not a SkyHanniKeyBind")
    }

    @HandleEvent
    private fun onInitFinished() = mappings.forEach { mapping ->
        val property = mapping.keyBind.option()
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
        val property = keyBind.option()
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
