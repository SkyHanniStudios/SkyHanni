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
    category: Category,
) : KeyMapping(
    KeyBindIds.mappingKey(options.category, options.name),
    keyBind.keyCode.toInputType(),
    keyBind.keyCode,
    category,
) {
    val boundKeyCode: Int get() = KeyMappingHelper.getBoundKeyOf(this).value

    fun isActive(): Boolean =
        keyBind.condition() && if (MinecraftCompat.screen == null) options.context.inGame else options.context.inScreen

    override fun setKey(key: InputConstants.Key) {
        super.setKey(key)
        pushKeyToConfig(key)
    }
}
