package at.hannibal2.skyhanni.utils.compat

import at.hannibal2.skyhanni.utils.SafeItemStack
import at.hannibal2.skyhanni.utils.guide.TabDef
import at.hannibal2.skyhanni.utils.guide.TabDef.Companion.TAB_LONG_SIDE
import at.hannibal2.skyhanni.utils.guide.TabDef.Companion.TAB_SHORT_SIDE
import at.hannibal2.skyhanni.utils.guide.TabDef.Companion.TAB_SPACING
import at.hannibal2.skyhanni.utils.guide.TabPage
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.primitives.empty

/**
 * Abstract base for SkyHanni screens with horizontal and/or vertical tab navigation.
 *
 * @param T The enum type used to identify pages.
 * @param defaultPage The page shown when the screen first opens.
 */
abstract class SkyHanniTabScreen<T : Enum<T>>(defaultPage: T) : SkyHanniChromeScreen() {

    abstract val pages: Map<T, TabPage>
    open val horizontalTabs: List<TabDef> = emptyList()
    open val verticalTabs: List<TabDef> = emptyList()

    protected var currentPage: T = defaultPage
        set(value) {
            pages[field]?.onLeave()
            pages[value]?.onEnter()
            field = value
            rebuildDisplay()
        }

    fun hTab(item: SafeItemStack, tip: Renderable, onClick: (TabDef) -> Unit) =
        TabDef(item, tip, isVertical = false, onClick = onClick)

    fun vTab(item: SafeItemStack, tip: Renderable, onClick: (TabDef) -> Unit) =
        TabDef(item, tip, isVertical = true, onClick = onClick)

    fun refreshPage() {
        pages[currentPage]?.refresh()
    }

    final override fun buildContent(): Renderable {
        val page = pages[currentPage] ?: return Renderable.empty()
        val pageRenderable = page.buildRenderable()
        return object : Renderable {
            override val width = pageRenderable.width
            override val height = pageRenderable.height
            override val horizontalAlign = pageRenderable.horizontalAlign
            override val verticalAlign = pageRenderable.verticalAlign

            override fun render(mouseOffsetX: Int, mouseOffsetY: Int) {
                renderTabs(horizontalTabs, vertical = false, mouseOffsetX, mouseOffsetY)
                renderTabs(verticalTabs, vertical = true, mouseOffsetX, mouseOffsetY)
                pageRenderable.render(mouseOffsetX, mouseOffsetY)
            }
        }
    }

    private fun renderTabs(tabs: List<TabDef>, vertical: Boolean, mouseOffsetX: Int, mouseOffsetY: Int) {
        val along = TAB_SPACING * 3f
        val across = -TAB_LONG_SIDE.toFloat()
        val shift = (TAB_SHORT_SIDE + TAB_SPACING).toFloat()
        val shiftX = if (vertical) 0f else shift
        val shiftY = if (vertical) shift else 0f
        var xOffset = if (vertical) across else along
        var yOffset = if (vertical) along else across
        DrawContextUtils.pushPop {
            DrawContextUtils.translate(xOffset, yOffset)
            for (tab in tabs) {
                tab.render(mouseOffsetX + xOffset.toInt(), mouseOffsetY + yOffset.toInt())
                xOffset += shiftX
                yOffset += shiftY
                DrawContextUtils.translate(shiftX, shiftY)
            }
        }
    }
}
