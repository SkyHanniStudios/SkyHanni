package at.hannibal2.skyhanni.utils.compat

import at.hannibal2.skyhanni.utils.GuiRenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.container.VerticalContainerRenderable.Companion.vertical
import at.hannibal2.skyhanni.utils.renderables.primitives.text

/**
 * Abstract base for SkyHanni content screens.
 *
 * Subclasses provide [screenTitle] and [buildContent]. The panel, title and separator
 * are rendered automatically. Call [rebuildDisplay]
 * whenever the content structure changes (e.g., mode switch, list mutation).
 *
 * @see SkyHanniCanvasScreen for full-canvas screens without chrome.
 */
abstract class SkyHanniChromeScreen : SkyHanniBaseScreen() {

    abstract val screenTitle: String

    /** Returns the Renderable representing this screen's content area. */
    abstract fun buildContent(): Renderable

    private var display: Renderable? = null
    private var lastWidth = 0
    private var lastHeight = 0

    /**
     * Rebuilds the full display Renderable (chrome + content).
     * Call this whenever the content structure changes.
     */
    fun rebuildDisplay() {
        display = buildChrome(buildContent())
    }

    private fun buildTitle(): Renderable = Renderable.text("§l$screenTitle")

    val maxContentWidth: Int get() = width * SCREEN_SHARE_NUMERATOR / SCREEN_SHARE_DENOMINATOR - SkyHanniScreenTheme.PANEL_PADDING * 2

    val maxContentHeight: Int
        get() = height * SCREEN_SHARE_NUMERATOR / SCREEN_SHARE_DENOMINATOR -
            SkyHanniScreenTheme.PANEL_PADDING * 2 - buildTitle().height - SEPARATOR_HEIGHT - BODY_SPACING * 2

    protected open fun renderPanel(render: () -> Unit) = render()

    private fun buildChrome(content: Renderable): Renderable {
        val titleText = buildTitle()
        val panelWidth = content.width.coerceAtLeast(titleText.width)
        val separator = object : Renderable {
            override val width = panelWidth
            override val height = SEPARATOR_HEIGHT
            override val horizontalAlign = HorizontalAlignment.LEFT
            override val verticalAlign = VerticalAlignment.TOP
            override fun render(mouseOffsetX: Int, mouseOffsetY: Int) {
                GuiRenderUtils.drawRect(0, 0, width, SEPARATOR_HEIGHT, SkyHanniScreenTheme.SEPARATOR_COLOR)
            }
        }
        val body = Renderable.vertical(
            listOf(titleText, separator, content),
            spacing = BODY_SPACING,
        )
        return Renderable.drawInsideDarkRect(body, padding = SkyHanniScreenTheme.PANEL_PADDING)
    }

    final override fun onDrawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground(mouseX, mouseY, partialTicks)
        if (lastWidth != width || lastHeight != height) {
            lastWidth = width
            lastHeight = height
            rebuildDisplay()
        }
        onChromeDrawScreen(mouseX, mouseY, partialTicks)
        val panel = display ?: return
        val startX = (width - panel.width) / 2
        val startY = (height - panel.height) / 2

        renderPanel {
            DrawContextUtils.pushPop {
                DrawContextUtils.translate(startX.toFloat(), startY.toFloat())
                Renderable.withMousePosition(mouseX - startX, mouseY - startY) {
                    panel.render(0, 0)
                }
            }
        }
    }

    final override fun onInitGui() {
        rebuildDisplay()
        onChromeInit()
    }

    /** Called after the screen initializes. Override instead of [onInitGui]. */
    open fun onChromeInit() {}

    /**
     * Called each draw frame after resize checks. Override to poll async state and call
     * [rebuildDisplay] when content needs to change (e.g., data finished loading).
     */
    open fun onChromeDrawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {}

    companion object {
        private const val SCREEN_SHARE_NUMERATOR = 4
        private const val SCREEN_SHARE_DENOMINATOR = 5
        private const val SEPARATOR_HEIGHT = 1
        private const val BODY_SPACING = 4
    }
}
