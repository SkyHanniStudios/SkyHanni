package at.hannibal2.skyhanni.utils.compat

import at.hannibal2.skyhanni.utils.GuiRenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.ScrollValue
import at.hannibal2.skyhanni.utils.renderables.primitives.text

object SkyHanniScreenTheme {

    const val PANEL_PADDING = 8
    const val SEPARATOR_COLOR = GuiRenderUtils.FLOATING_RECT_DARK_BORDER
    private const val BUTTON_PADDING = 3

    fun buildButton(label: String, onClick: () -> Unit): Renderable {
        val text = Renderable.text(label)
        return Renderable.clickable(
            Renderable.hoverable(
                padded(text, BUTTON_PADDING) { w, h -> GuiRenderUtils.drawFloatingRectDark(0, 0, w, h, shadow = true) },
                padded(text, BUTTON_PADDING) { w, h -> GuiRenderUtils.drawFloatingRectDark(0, 0, w, h, shadow = false) },
                bypassChecks = true,
            ),
            onLeftClick = onClick,
            bypassChecks = true,
        )
    }

    fun hoverRow(
        content: Renderable,
        padding: Int = 2,
        idleBackground: Boolean = false,
        onHover: () -> Unit = {},
    ): Renderable = Renderable.hoverable(
        padded(content, padding) { w, h -> GuiRenderUtils.drawFloatingRectLight(0, 0, w, h, shadow = false) },
        padded(content, padding) { w, h ->
            if (idleBackground) GuiRenderUtils.drawFloatingRectDark(0, 0, w, h, shadow = false)
        },
        bypassChecks = true,
        onHover = onHover,
    )

    fun scrollList(
        list: List<Renderable>,
        height: Int,
        scrollValue: ScrollValue,
        velocity: Double = 2.0,
        button: Int? = null,
        bypassChecks: Boolean = false,
    ): Renderable = Renderable.scrollList(
        list,
        height,
        scrollValue = scrollValue,
        velocity = velocity,
        button = button,
        bypassChecks = bypassChecks,
        showScrollbar = true,
    )

    private fun padded(content: Renderable, padding: Int, background: (Int, Int) -> Unit) = object : Renderable {
        override val width = content.width + padding * 2
        override val height = content.height + padding * 2
        override val horizontalAlign = HorizontalAlignment.LEFT
        override val verticalAlign = VerticalAlignment.TOP

        override fun render(mouseOffsetX: Int, mouseOffsetY: Int) {
            background(width, height)
            DrawContextUtils.translate(padding.toFloat(), padding.toFloat())
            content.render(mouseOffsetX + padding, mouseOffsetY + padding)
            DrawContextUtils.translate(-padding.toFloat(), -padding.toFloat())
        }
    }
}
