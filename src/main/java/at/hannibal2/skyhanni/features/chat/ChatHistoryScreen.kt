package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod.launch
import at.hannibal2.skyhanni.data.ChatManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ClipboardUtils
import at.hannibal2.skyhanni.utils.GuiRenderUtils
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.StringUtils.stripHypixelMessage
import at.hannibal2.skyhanni.utils.compat.SkyHanniChromeScreen
import at.hannibal2.skyhanni.utils.compat.SkyHanniScreenTheme
import at.hannibal2.skyhanni.utils.compat.convertToJsonString
import at.hannibal2.skyhanni.utils.compat.formattedTextCompat
import at.hannibal2.skyhanni.utils.coroutines.CoroutineSettings
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableTooltips
import at.hannibal2.skyhanni.utils.renderables.ScrollValue
import at.hannibal2.skyhanni.utils.renderables.container.HorizontalContainerRenderable.Companion.horizontal
import at.hannibal2.skyhanni.utils.renderables.container.VerticalContainerRenderable.Companion.vertical
import at.hannibal2.skyhanni.utils.renderables.primitives.StringRenderable
import at.hannibal2.skyhanni.utils.renderables.primitives.empty
import at.hannibal2.skyhanni.utils.renderables.primitives.text
import net.minecraft.client.gui.components.ComponentRenderUtils
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence

class ChatHistoryScreen(
    private val history: List<ChatManager.MessageFilteringResult>,
) : SkyHanniChromeScreen() {

    override val screenTitle = "Chat History"
    private val scrollValue = ScrollValue()

    private val ChatManager.MessageFilteringResult.reason: String? get() = actionReason ?: modifiedReason

    override fun buildContent(): Renderable {
        val reasonMaxLength = history.maxOfOrNull { msg -> msg.reason?.let { mc.font.width(it) } ?: 0 } ?: 0
        val xOffset = ChatManager.ActionKind.maxLength + reasonMaxLength + 10
        val wrapWidth = LIST_WIDTH.coerceAtMost(maxContentWidth) - xOffset - SCROLLBAR_WIDTH

        val rows = history.map { buildRow(it, wrapWidth, xOffset) }
        val listHeight = LIST_HEIGHT.coerceAtMost(maxContentHeight)
        return SkyHanniScreenTheme.scrollList(rows, listHeight, scrollValue, velocity = 12.0, bypassChecks = true)
    }

    private fun buildRow(msg: ChatManager.MessageFilteringResult, wrapWidth: Int, xOffset: Int): Renderable {
        val tooltip = msg.hoverInfo.takeIf { it.isNotEmpty() }?.map(StringRenderable::from)
        val extraTooltip = msg.hoverExtraInfo.takeIf { it.isNotEmpty() }?.map(StringRenderable::from)

        val row = SkyHanniScreenTheme.hoverRow(buildRowContent(msg, wrapWidth, xOffset)) {
            val tip = if (KeyboardManager.isShiftKeyDown()) extraTooltip ?: tooltip else tooltip
            tip?.let { RenderableTooltips.setTooltipForRender(it) }
        }

        return Renderable.clickable(
            row,
            onLeftClick = {
                copyCoroutine.launch {
                    val (target, copyType) = if (KeyboardManager.isShiftKeyDown()) {
                        msg.message.convertToJsonString() to "structured"
                    } else {
                        msg.message.formattedTextCompat().stripHypixelMessage() to ""
                    }
                    val copied = ClipboardUtils.copyToClipboardAsync(target).await() ?: false
                    if (!copied) ChatUtils.chat("Failed to copy $copyType to clipboard")
                    else ChatUtils.chat("Copied $copyType chat line to clipboard")
                }
            },
            bypassChecks = true,
        )
    }

    private fun buildRowContent(msg: ChatManager.MessageFilteringResult, wrapWidth: Int, xOffset: Int): Renderable {
        val actionLabel = Renderable.fixedSizeLine(
            Renderable.text(msg.actionKind.renderedString),
            width = ChatManager.ActionKind.maxLength,
        )
        val reasonLabel = Renderable.fixedSizeLine(
            msg.reason?.let { Renderable.text(it) } ?: Renderable.empty(),
            width = xOffset - ChatManager.ActionKind.maxLength,
        )
        val messageBlock = wrappedLines(msg.message, wrapWidth)
        val msgBlock = msg.modified?.let {
            Renderable.vertical(
                listOf(messageBlock, Renderable.text("§e§lNEW TEXT"), wrappedLines(it, wrapWidth)),
                spacing = 1,
            )
        } ?: messageBlock
        return Renderable.horizontal(listOf(actionLabel, reasonLabel, msgBlock), spacing = 0)
    }

    private fun wrappedLines(component: Component, wrapWidth: Int): Renderable = Renderable.vertical(
        ComponentRenderUtils.wrapComponents(component, wrapWidth, mc.font).map { formattedCharRenderable(it, wrapWidth) },
        spacing = 1,
    )

    private fun formattedCharRenderable(line: FormattedCharSequence, wrapWidth: Int): Renderable = object : Renderable {
        override val width = wrapWidth
        override val height = 10
        override val horizontalAlign = HorizontalAlignment.LEFT
        override val verticalAlign = VerticalAlignment.TOP

        override fun render(mouseOffsetX: Int, mouseOffsetY: Int) {
            GuiRenderUtils.drawString(line, 0, 0, -1)
        }
    }

    companion object {
        private const val LIST_WIDTH = 600
        private const val LIST_HEIGHT = 300
        private const val SCROLLBAR_WIDTH = 7
        private val copyCoroutine = CoroutineSettings("chat history copy to clipboard")
    }
}
