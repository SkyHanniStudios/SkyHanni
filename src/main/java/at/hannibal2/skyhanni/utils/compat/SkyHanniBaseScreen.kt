package at.hannibal2.skyhanni.utils.compat

import at.hannibal2.skyhanni.test.command.ErrorManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

@Suppress("UnusedParameter", "TooManyFunctions")
abstract class SkyHanniBaseScreen(title: Component = Component.empty()) : Screen(title) {

    val mc: Minecraft = Minecraft.getInstance()

    private inline fun guarded(action: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(e, "Error while $action", "screen" to this)
        }
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        super.extractRenderState(context, mouseX, mouseY, delta)
        DrawContextUtils.setContext(context)
        guarded("drawing screen") { onDrawScreen(mouseX, mouseY, delta) }
        DrawContextUtils.clearContext()
    }

    override fun extractBackground(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, deltaTicks: Float) =
        guarded("rendering background") { extractMenuBackground(context) }

    open fun onDrawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {}

    override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        guarded("clicking mouse") { onMouseClicked(click.x.toInt(), click.y.toInt(), click.button()) }
        postHandleMouseInput()
        return super.mouseClicked(click, doubled)
    }

    open fun onMouseClicked(originalMouseX: Int, originalMouseY: Int, mouseButton: Int) {}

    override fun keyPressed(input: KeyEvent): Boolean {
        guarded("typing key") { onKeyTyped(null, input.key) }
        return super.keyPressed(input)
    }

    override fun charTyped(input: CharacterEvent): Boolean {
        guarded("typing key") { onKeyTyped(input.codepoint.toChar(), null) }
        return super.charTyped(input)
    }

    open fun onKeyTyped(typedChar: Char?, keyCode: Int?) {}

    override fun mouseReleased(click: MouseButtonEvent): Boolean {
        guarded("releasing mouse") { onMouseReleased(click.x.toInt(), click.y.toInt(), click.button()) }
        postHandleMouseInput()
        return super.mouseReleased(click)
    }

    open fun onMouseReleased(originalMouseX: Int, originalMouseY: Int, state: Int) {}

    override fun mouseDragged(click: MouseButtonEvent, mouseX: Double, mouseY: Double): Boolean {
        // TODO idk if mouseX is correct or if it should be click.x
        guarded("clicking and moving mouse") { onMouseClickMove(mouseX.toInt(), mouseY.toInt(), click.button(), 0L) }
        postHandleMouseInput()
        return super.mouseDragged(click, mouseX, mouseY)
    }

    open fun onMouseClickMove(originalMouseX: Int, originalMouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {}

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        postHandleMouseInput()
        super.mouseMoved(mouseX, mouseY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        postHandleMouseInput()
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    private fun postHandleMouseInput() = guarded("handling mouse input") { onHandleMouseInput() }

    open fun onHandleMouseInput() {}

    override fun onClose() {
        super.onClose()
        guarded("closing GUI") { guiClosed() }
    }

    open fun guiClosed() {}

    override fun init() {
        super.init()
        guarded("initializing GUI") { onInitGui() }
    }

    open fun onInitGui() {}

    fun drawDefaultBackground(mouseX: Int, mouseY: Int, partialTicks: Float) {
        extractMenuBackground(DrawContextUtils.drawContext)
    }

    open fun shouldShowItemList(): Boolean = false
}
