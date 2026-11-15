package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.KeyboardManager.toInputType
import at.hannibal2.skyhanni.utils.SkyHanniKeyBindManager.pushKeyToConfig
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping

class SkyHanniKeyMapping(
    val registration: KeyBindRegistration,
    val keyBind: SkyHanniKeyBind,
    category: KeyMapping.Category,
) : KeyMapping(
    registration.mappingKey,
    keyBind.option().get().toInputType(),
    keyBind.option().get(),
    category,
) {
    val boundKeyCode: Int get() = KeyMappingHelper.getBoundKeyOf(this).value

    fun isActive(): Boolean =
        keyBind.condition() && if (MinecraftCompat.screen == null) registration.inGame else registration.inScreen

    override fun setKey(key: InputConstants.Key) {
        super.setKey(key)
        pushKeyToConfig(key)
    }
}
