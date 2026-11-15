package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.KeyboardManager.toInputType
import at.hannibal2.skyhanni.utils.SkyHanniKeyBindManager.pushKeyToConfig
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping

class SkyHanniKeyMapping(
    val options: RegisterKeyBind,
    val keyBind: SkyHanniKeyBind,
    category: KeyMapping.Category,
) : KeyMapping(
    KeyBindIds.mappingKey(options.category, options.name),
    keyBind.option().get().toInputType(),
    keyBind.option().get(),
    category,
) {
    val boundKeyCode: Int get() = KeyMappingHelper.getBoundKeyOf(this).value

    fun isActive(): Boolean = keyBind.condition() && when (options.context) {
        KeyBindContext.IN_GAME -> MinecraftCompat.screen == null
        KeyBindContext.IN_SCREEN -> MinecraftCompat.screen != null
        KeyBindContext.ALL -> true
    }

    override fun setKey(key: InputConstants.Key) {
        super.setKey(key)
        pushKeyToConfig(key)
    }
}
