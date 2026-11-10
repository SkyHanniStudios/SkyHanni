package at.hannibal2.skyhanni.events.render.gui

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import net.minecraft.client.gui.screens.Screen

@PrimaryFunction("onGuiMouseInput")
class GuiMouseInputEvent(val gui: Screen) : CancellableSkyHanniEvent()
