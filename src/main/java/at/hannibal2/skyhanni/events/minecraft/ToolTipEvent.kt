package at.hannibal2.skyhanni.events.minecraft

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import at.hannibal2.skyhanni.utils.SafeItemStack
import net.minecraft.world.inventory.Slot

/**
 * Use [ToolTipTextEvent] Instead
 */
@Deprecated("Use ToolTipTextEvent instead", ReplaceWith("ToolTipTextEvent"))
@Suppress("MissingPrimaryFunction")
class ToolTipEvent(val slot: Slot, val itemStack: SafeItemStack, val toolTip: MutableList<String>) : CancellableSkyHanniEvent()
