package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.SkyHanniConfig
import com.mojang.blaze3d.platform.InputConstants
import io.github.notenoughupdates.moulconfig.observer.Property

/**
 * A SkyHanni config keybind that is exposed in Minecraft's controls screen and kept in sync with MoulConfig.
 *
 * Declare it as an `internal val` annotated with [RegisterKeyBind] inside a `@SkyHanniModule` object.
 * The annotation carries the display name, category, context and trigger.
 *
 * @param configOption Selects the config `Property<Int>` backing this keybind.
 * @param condition Returns true when this keybind should be active and shown in the controls screen.
 * @param onTrigger Runs when the bound key fires in an active context.
 */
class SkyHanniKeyBind(
    val configOption: (SkyHanniConfig) -> Property<Int>,
    val condition: () -> Boolean = { true },
    val onTrigger: () -> Unit,
) {
    val property: Property<Int> get() = configOption(SkyHanniMod.feature)
    val keyCode: Int get() = property.get()
    val isBound: Boolean get() = keyCode != InputConstants.UNKNOWN.value
}
