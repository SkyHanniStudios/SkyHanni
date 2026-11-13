package at.hannibal2.skyhanni.utils.compat

import at.hannibal2.skyhanni.test.command.ErrorManager
import net.minecraft.client.gui.GuiGraphicsExtractor

interface SkyHanniScreenHooks {

    fun onDrawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {}

    fun guarded(action: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(e, "Error while $action", "screen" to this)
        }
    }

    fun drawGuarded(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        DrawContextUtils.setContext(context)
        guarded("drawing screen") { onDrawScreen(mouseX, mouseY, partialTicks) }
        DrawContextUtils.clearContext()
    }
}
