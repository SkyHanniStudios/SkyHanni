package at.hannibal2.skyhanni.features.misc.massconfiguration

import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import at.hannibal2.skyhanni.utils.compat.SkyHanniScreenTheme
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableTooltips
import at.hannibal2.skyhanni.utils.renderables.container.HorizontalContainerRenderable.Companion.horizontal
import at.hannibal2.skyhanni.utils.renderables.container.VerticalContainerRenderable.Companion.vertical
import at.hannibal2.skyhanni.utils.renderables.labeledCard
import at.hannibal2.skyhanni.utils.renderables.primitives.text

object DefaultConfigGui {

    private const val CARD_WIDTH = 380
    private const val LIST_HEIGHT = 250

    fun buildContent(screen: DefaultConfigScreen): Renderable {
        val categoryCards = screen.orderedOptions.entries.map { (cat, _) ->
            buildCategoryCard(screen, cat)
        }

        val list = SkyHanniScreenTheme.scrollList(categoryCards, LIST_HEIGHT, screen.scrollValue, button = 0)

        val actionBar = buildActionBar(screen)
        return Renderable.vertical(listOf(list, actionBar), spacing = 8)
    }

    private fun buildCategoryCard(screen: DefaultConfigScreen, cat: Category): Renderable {
        val state = screen.resetSuggestionState[cat] ?: ResetSuggestionState.LEAVE_DEFAULTS
        val card = Renderable.labeledCard(
            "§e${cat.name} ${state.label}",
            Renderable.text("§7${cat.description}"),
            CARD_WIDTH,
        )

        val normalTooltip = buildList {
            add(Renderable.text("§e${cat.name}"))
            add(Renderable.text("§7${cat.description}"))
            add(Renderable.text("§7Current plan: ${state.label}"))
            add(Renderable.text("§aClick to toggle!"))
            add(Renderable.text("§7Hold shift to show all options"))
        }
        val shiftTooltip = buildList {
            add(Renderable.text("§e${cat.name}"))
            add(Renderable.text("§7${cat.description}"))
            addAll(screen.orderedOptions[cat]?.map { Renderable.text("§7 - §a${it.name}") }.orEmpty())
        }

        val row = SkyHanniScreenTheme.hoverRow(card, padding = 3, radius = 5, idleBackground = true) {
            RenderableTooltips.setTooltipForRender(if (KeyboardManager.isShiftKeyDown()) shiftTooltip else normalTooltip)
        }

        return Renderable.clickable(
            row,
            onLeftClick = {
                screen.setState(cat, state.next)
                screen.rebuildDisplay()
            },
            bypassChecks = true,
        )
    }

    private fun DefaultConfigScreen.setState(cat: Category, state: ResetSuggestionState) {
        resetSuggestionState[cat] = state
        orderedOptions[cat]?.forEach { it.toggleOverride = null }
    }

    private fun setAllButton(screen: DefaultConfigScreen, label: String, state: ResetSuggestionState) =
        SkyHanniScreenTheme.buildButton(label, SkyHanniScreenTheme.COLOR_BTN_NEUTRAL) {
            screen.resetSuggestionState.keys.toList().forEach { screen.setState(it, state) }
            screen.rebuildDisplay()
        }

    private fun buildActionBar(screen: DefaultConfigScreen): Renderable {
        val applyBtn = SkyHanniScreenTheme.buildButton("Apply choices", SkyHanniScreenTheme.COLOR_BTN_PRIMARY) {
            DefaultConfigFeatures.applyCategorySelections(screen.resetSuggestionState, screen.orderedOptions)
            MinecraftCompat.screen = null
        }
        val cancelBtn = SkyHanniScreenTheme.buildButton("Cancel", SkyHanniScreenTheme.COLOR_BTN_DANGER) {
            MinecraftCompat.screen = null
        }

        return Renderable.horizontal(
            listOf(
                applyBtn,
                setAllButton(screen, "Turn all on", ResetSuggestionState.TURN_ALL_ON),
                setAllButton(screen, "Turn all off", ResetSuggestionState.TURN_ALL_OFF),
                setAllButton(screen, "Leave all untouched", ResetSuggestionState.LEAVE_DEFAULTS),
                cancelBtn,
            ),
            spacing = 10,
        )
    }
}
