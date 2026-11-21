package at.hannibal2.skyhanni.events.render.gui

import at.hannibal2.skyhanni.api.event.RenderingSkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen

@PrimaryFunction("onScreenDrawn")
class ScreenDrawnEvent(context: GuiGraphicsExtractor, val gui: Screen?) : RenderingSkyHanniEvent(context)
