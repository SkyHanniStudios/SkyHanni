package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.RenderingSkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.utils.SafeItemStack
import net.minecraft.client.gui.GuiGraphicsExtractor

@PrimaryFunction("onRenderItem")
class RenderGuiItemOverlayEvent(
    context: GuiGraphicsExtractor,
    val stack: SafeItemStack?,
    val x: Int,
    val y: Int,
) : RenderingSkyHanniEvent(context)
