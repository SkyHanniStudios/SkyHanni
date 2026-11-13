package at.hannibal2.skyhanni.features.misc.visualwords

import at.hannibal2.skyhanni.data.model.TextInput
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.StringUtils.convertToFormatted
import at.hannibal2.skyhanni.utils.compat.ColoredBlockCompat
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import at.hannibal2.skyhanni.utils.compat.SkyHanniScreenTheme
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.container.HorizontalContainerRenderable.Companion.horizontal
import at.hannibal2.skyhanni.utils.renderables.container.VerticalContainerRenderable.Companion.vertical
import at.hannibal2.skyhanni.utils.renderables.primitives.ItemStackRenderable.Companion.item
import at.hannibal2.skyhanni.utils.renderables.primitives.text
import at.hannibal2.skyhanni.utils.renderables.primitives.textInput
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment as HA
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment as VA

/**
 * Constructs the full Renderable display for [VisualWordScreen].
 *
 * Stateless. All mutable state lives in [VisualWordScreen].
 */
object VisualWordGui {

    private val upSkull = ItemUtils.repoSkullProvider(
        displayName = "§aMove Up",
        uuid = "7f68dd73-1ff6-4193-b246-820975d6fab1",
        repoSkullId = "UP_ARROW",
    )
    private val downSkull = ItemUtils.repoSkullProvider(
        displayName = "§aMove Down",
        uuid = "e4ace6de-0629-4719-aea3-3e113314dd3f",
        repoSkullId = "DOWN_ARROW",
    )
    private val upItem by lazy { Renderable.item(upSkull) { scale = 1.0 } }
    private val upItemDimmed by lazy {
        Renderable.item(upSkull) {
            scale = 1.0
            alpha = 0.4f
        }
    }
    private val downItem by lazy { Renderable.item(downSkull) { scale = 1.0 } }
    private val downItemDimmed by lazy {
        Renderable.item(downSkull) {
            scale = 1.0
            alpha = 0.4f
        }
    }

    fun isInGui(): Boolean = MinecraftCompat.screen is VisualWordScreen

    /**
     * Constructs the full display [Renderable] for the current state of [screen].
     * Stateless. All mutable state lives in [VisualWordScreen].
     */
    fun buildDisplay(screen: VisualWordScreen): Renderable =
        if (screen.currentlyEditing) buildEditView(screen) else buildListView(screen)

    private fun buildListView(screen: VisualWordScreen): Renderable {
        val header = Renderable.text("§bVisual Word Replacements", scale = 1.2, horizontalAlign = HA.CENTER)
        val subtitle = Renderable.text(
            "§7Each entry replaces §bPhrase§7 with §aReplacement",
            scale = 0.85,
            horizontalAlign = HA.CENTER,
        )

        val listArea = if (screen.modifiedWords.isEmpty()) {
            Renderable.text("§7No entries yet. Click §aAdd New§7 to start.", horizontalAlign = HA.CENTER)
        } else {
            SkyHanniScreenTheme.scrollList(
                screen.modifiedWords.mapIndexed { index, word -> buildWordRow(screen, index, word) },
                height = 150,
                scrollValue = screen.listScrollValue,
                velocity = 4.0,
                bypassChecks = true,
            )
        }

        return Renderable.vertical(
            listOf(header, subtitle, listArea, buildListBottomRow(screen)),
            spacing = 6,
            horizontalAlign = HA.CENTER,
        )
    }

    private fun buildWordRow(screen: VisualWordScreen, index: Int, word: VisualWord): Renderable {
        val indexLabel = Renderable.fixedSizeLine(Renderable.text("§7${index + 1}."), width = 20)

        val textFixed = Renderable.fixedSizeLine(
            Renderable.vertical(
                listOf(
                    Renderable.text(word.phrase.convertToFormatted()),
                    Renderable.text(word.replacement.convertToFormatted()),
                ),
                spacing = 1,
            ),
            width = 230,
        )

        val upBtn = if (index > 0) Renderable.clickable(
            upItem.withTip(),
            onLeftClick = { screen.moveWord(index, up = true) },
            bypassChecks = true,
        ) else upItemDimmed.withTip()

        val downBtn = if (index < screen.modifiedWords.lastIndex) Renderable.clickable(
            downItem.withTip(),
            onLeftClick = { screen.moveWord(index, up = false) },
            bypassChecks = true,
        ) else downItemDimmed.withTip()

        val statusItem = if (word.enabled) ColoredBlockCompat.GREEN.createStainedClay()
        else ColoredBlockCompat.RED.createStainedClay()

        val statusBtn = Renderable.clickable(
            Renderable.item(statusItem) { scale = 0.9 },
            onLeftClick = { screen.toggleEnabled(index) },
            bypassChecks = true,
        )

        val clickableText = Renderable.clickable(
            textFixed,
            onLeftClick = { screen.enterEditMode(index) },
            bypassChecks = true,
        )
        val rowContent = Renderable.horizontal(
            listOf(indexLabel, clickableText, upBtn, downBtn, statusBtn),
            spacing = 4,
            verticalAlign = VA.CENTER,
        )
        return SkyHanniScreenTheme.hoverRow(rowContent, padding = 3, radius = 5, idleBackground = true)
    }

    private fun buildListBottomRow(screen: VisualWordScreen): Renderable =
        SkyHanniScreenTheme.buildButton("§a+ Add New", SkyHanniScreenTheme.COLOR_BTN_PRIMARY) { screen.addNewWord() }

    private fun buildEditView(screen: VisualWordScreen): Renderable {
        val word = screen.modifiedWords.getOrNull(screen.currentIndex) ?: return buildListView(screen)

        val header = Renderable.text("§bEdit Replacement", scale = 1.2, horizontalAlign = HA.CENTER)
        val hint = Renderable.text(
            "§8Tip: use \"&&\" to produce the §r§8Minecraft formatting character §r§8(e.g. &&aGreen)",
            scale = 0.8,
        )

        val toggleRow = Renderable.horizontal(
            listOf(
                SkyHanniScreenTheme.buildButton(
                    if (word.enabled) "§a● Enabled" else "§c● Disabled",
                    if (word.enabled) SkyHanniScreenTheme.COLOR_BTN_PRIMARY else SkyHanniScreenTheme.COLOR_BTN_DANGER,
                ) { screen.toggleEnabled(screen.currentIndex) },
                SkyHanniScreenTheme.buildButton(
                    if (word.isCaseSensitive()) "§bCase Sensitive" else "§7Case Insensitive",
                    SkyHanniScreenTheme.COLOR_BTN_NEUTRAL,
                ) { screen.toggleCaseSensitive(screen.currentIndex) },
            ),
            spacing = 8,
            verticalAlign = VA.CENTER,
        )

        val actionRow = Renderable.horizontal(
            listOf(
                SkyHanniScreenTheme.buildButton("§c✗ Delete", SkyHanniScreenTheme.COLOR_BTN_DANGER) {
                    screen.deleteWord(screen.currentIndex)
                },
                SkyHanniScreenTheme.buildButton("§e← Back", SkyHanniScreenTheme.COLOR_BTN_WARNING) { screen.exitEditMode() },
            ),
            spacing = 8,
            verticalAlign = VA.CENTER,
        )

        return Renderable.vertical(
            listOf(
                header,
                buildFieldSection(screen, "§bPhrase §7(the text to be replaced):", screen.phraseInput),
                buildFieldSection(screen, "§aReplacement §7(what to replace it with):", screen.replacementInput),
                hint,
                toggleRow,
                actionRow,
            ),
            spacing = 8,
            horizontalAlign = HA.CENTER,
        )
    }

    private fun buildFieldSection(screen: VisualWordScreen, label: String, textInput: TextInput) =
        Renderable.vertical(
            listOf(
                Renderable.text(label),
                Renderable.textInput(
                    textInput,
                    isActive = { screen.activeInput === textInput },
                    onActivate = { screen.activeInput = textInput },
                    width = 310,
                ),
            ),
            spacing = 3,
        )
}
