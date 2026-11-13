package at.hannibal2.skyhanni.features.misc.massconfiguration

import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import at.hannibal2.skyhanni.utils.compat.SkyHanniChromeScreen
import at.hannibal2.skyhanni.utils.compat.SkyHanniScreenTheme
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableTooltips
import at.hannibal2.skyhanni.utils.renderables.ScrollValue
import at.hannibal2.skyhanni.utils.renderables.container.HorizontalContainerRenderable.Companion.horizontal
import at.hannibal2.skyhanni.utils.renderables.container.VerticalContainerRenderable.Companion.vertical
import at.hannibal2.skyhanni.utils.renderables.labeledCard
import at.hannibal2.skyhanni.utils.renderables.primitives.text

class DefaultConfigScreen(
    private val orderedOptions: Map<Category, List<FeatureToggleableOption>>,
    old: String,
    new: String,
) : SkyHanniChromeScreen() {

    override val screenTitle: String = when {
        old == "null" && new == "null" -> "SkyHanni Default Options"
        old == "null" -> "SkyHanni Options In Version $new"
        new == "null" -> "SkyHanni Options since $old"
        else -> "SkyHanni Options $old → $new"
    }

    private val scrollValue = ScrollValue()
    private val resetSuggestionState: MutableMap<Category, ResetSuggestionState> =
        orderedOptions.keys.associateWith { ResetSuggestionState.LEAVE_DEFAULTS }.toMutableMap()

    override fun buildContent(): Renderable {
        val actionBar = buildActionBar()
        val listHeight = LIST_HEIGHT.coerceAtMost(maxContentHeight - actionBar.height - CONTENT_SPACING)
        val list = SkyHanniScreenTheme.scrollList(orderedOptions.keys.map(::buildCategoryCard), listHeight, scrollValue, button = 0)
        return Renderable.vertical(listOf(list, actionBar), spacing = CONTENT_SPACING)
    }

    private fun buildCategoryCard(cat: Category): Renderable {
        val state = resetSuggestionState[cat] ?: ResetSuggestionState.LEAVE_DEFAULTS
        val card = Renderable.labeledCard(
            "§e${cat.name} ${state.label}",
            Renderable.text("§7${cat.description}"),
            CARD_WIDTH,
        )

        val normalTooltip = listOf(
            Renderable.text("§e${cat.name}"),
            Renderable.text("§7${cat.description}"),
            Renderable.text("§7Current plan: ${state.label}"),
            Renderable.text("§aClick to toggle!"),
            Renderable.text("§7Hold shift to show all options"),
        )
        val shiftTooltip = listOf(
            Renderable.text("§e${cat.name}"),
            Renderable.text("§7${cat.description}"),
        ) + orderedOptions[cat]?.map { Renderable.text("§7 - §a${it.name}") }.orEmpty()

        val row = SkyHanniScreenTheme.hoverRow(card, padding = 3, radius = 5, idleBackground = true) {
            RenderableTooltips.setTooltipForRender(if (KeyboardManager.isShiftKeyDown()) shiftTooltip else normalTooltip)
        }

        return Renderable.clickable(
            row,
            onLeftClick = {
                setState(cat, state.next)
                rebuildDisplay()
            },
            bypassChecks = true,
        )
    }

    private fun setState(cat: Category, state: ResetSuggestionState) {
        resetSuggestionState[cat] = state
        orderedOptions[cat]?.forEach { it.toggleOverride = null }
    }

    private fun setAllButton(label: String, state: ResetSuggestionState) =
        SkyHanniScreenTheme.buildButton(label, SkyHanniScreenTheme.COLOR_BTN_NEUTRAL) {
            orderedOptions.keys.forEach { setState(it, state) }
            rebuildDisplay()
        }

    private fun buildActionBar(): Renderable {
        val applyBtn = SkyHanniScreenTheme.buildButton("Apply choices", SkyHanniScreenTheme.COLOR_BTN_PRIMARY) {
            DefaultConfigFeatures.applyCategorySelections(resetSuggestionState, orderedOptions)
            MinecraftCompat.screen = null
        }
        val cancelBtn = SkyHanniScreenTheme.buildButton("Cancel", SkyHanniScreenTheme.COLOR_BTN_DANGER) {
            MinecraftCompat.screen = null
        }

        return Renderable.horizontal(
            listOf(
                applyBtn,
                setAllButton("Turn all on", ResetSuggestionState.TURN_ALL_ON),
                setAllButton("Turn all off", ResetSuggestionState.TURN_ALL_OFF),
                setAllButton("Leave all untouched", ResetSuggestionState.LEAVE_DEFAULTS),
                cancelBtn,
            ),
            spacing = 10,
        )
    }

    companion object {
        private const val CARD_WIDTH = 380
        private const val LIST_HEIGHT = 250
        private const val CONTENT_SPACING = 8
    }
}
