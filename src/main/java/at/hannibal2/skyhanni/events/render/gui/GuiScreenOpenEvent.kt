package at.hannibal2.skyhanni.events.render.gui

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import net.minecraft.client.gui.screens.Screen

@PrimaryFunction("onGuiOpen")
class GuiScreenOpenEvent(val gui: Screen?) : SkyHanniEvent()
