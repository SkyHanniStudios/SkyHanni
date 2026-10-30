package at.hannibal2.skyhanni.features.inventory.wardrobe

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.config.features.inventory.customwardrobe.CustomWardrobeConfig
import at.hannibal2.skyhanni.features.inventory.wardrobe.AbstractWardrobeApi.Companion.MAX_SLOT_PER_PAGE
import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValue
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.AbstractCustomMenu
import at.hannibal2.skyhanni.utils.ColorUtils
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.ColorUtils.darker
import at.hannibal2.skyhanni.utils.ColorUtils.toColor
import at.hannibal2.skyhanni.utils.ConditionalUtils.transformIf
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SafeItemStack
import at.hannibal2.skyhanni.utils.SkyBlockUtils
import at.hannibal2.skyhanni.utils.compat.DrawContextUtils
import at.hannibal2.skyhanni.utils.compat.getTooltip
import at.hannibal2.skyhanni.utils.compat.getTooltipCompat
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.container.HorizontalContainerRenderable.Companion.horizontal
import at.hannibal2.skyhanni.utils.renderables.container.VerticalContainerRenderable.Companion.vertical
import at.hannibal2.skyhanni.utils.renderables.primitives.WrappedStringRenderable.Companion.wrappedText
import at.hannibal2.skyhanni.utils.renderables.primitives.placeholder
import at.hannibal2.skyhanni.utils.renderables.primitives.text
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import java.awt.Color

@Suppress("TooManyFunctions")
abstract class AbstractCustomWardrobe(
    val api: AbstractWardrobeApi,
    guiName: String,
) : AbstractCustomMenu(guiName) {

    val config: CustomWardrobeConfig get() = loadoutConfig.wardrobe

    protected abstract fun isFeatureEnabled(): Boolean

    protected abstract fun jumpToConfig()

    /** Renders the content of one slot, drawn below the invisible item hover areas. */
    protected abstract fun createSlotContentRenderable(
        slot: WardrobeSlot,
        playerWidth: Double,
        containerHeight: Int,
        containerWidth: Int,
    ): Renderable

    private var displayRenderable: Renderable? = null
    private var waitingForInventoryUpdate = false

    private val position: Position = Position().ignoreScale()
    private val loadingPosition: Position = Position().ignoreScale()

    private var renderableTopCorner: Pair<Int, Int> = 0 to 0
    private var renderableDimensions: Pair<Int, Int> = 0 to 0

    override val rectangle
        get() = ScreenRectangle(
            renderableTopCorner.first,
            renderableTopCorner.second,
            renderableDimensions.first,
            renderableDimensions.second,
        )

    override fun shouldReplace(inventoryName: String): Boolean {
        return isEnabled() && api.matchesInventoryName(inventoryName)
    }

    override fun shouldShowItemList(): Boolean = config.showReiItems

    override fun handleKeybinds() = CustomWardrobeKeybinds.handlePress(this)

    override fun renderOverlay(screenWidth: Int, screenHeight: Int) {
        if (checkScreenSize(screenWidth to screenHeight)) return

        val renderable = displayRenderable ?: return

        if (waitingForInventoryUpdate && config.loadingText) {
            val loadingRenderable = Renderable.text(
                "§cLoading...",
                scale = activeScale,
            )
            loadingPosition
                .moveTo(
                    position.x + (renderable.width - loadingRenderable.width) / 2,
                    position.y - loadingRenderable.height,
                )
                .renderRenderable(
                    loadingRenderable,
                    posLabel = guiName,
                    addToGuiManager = false,
                )
        }

        DrawContextUtils.translatedPushPopResult(0f, 0f) {
            position.renderRenderable(renderable, posLabel = guiName, addToGuiManager = false)
            if (EstimatedItemValue.config.enabled) {
                DrawContextUtils.translate(0f, 0f)
                EstimatedItemValue.tryRendering()
            }
        }
    }

    override fun onInventoryUpdate() {
        waitingForInventoryUpdate = false
        update()
    }

    override fun update() {
        displayRenderable = createRenderables()
        lastScreenSize?.let { updateRenderablePosition(it) }
    }

    override fun updateScreenSize(gui: Pair<Int, Int>): Boolean {
        val renderable = currentMaxSize ?: run {
            activeScale = loadoutConfig.spacing.globalScale / 100.0
            update()
            updateRenderablePosition(gui)
            return true
        }
        val scaleChanged = fitScale(gui, renderable)
        if (scaleChanged) update()

        updateRenderablePosition(gui)

        return scaleChanged
    }

    private fun updateRenderablePosition(gui: Pair<Int, Int>) {
        val renderable = displayRenderable ?: return

        val (width, height) = renderable.width to renderable.height
        renderableDimensions = width to height

        val left = (gui.first - width) / 2
        val top = (gui.second - height) / 2

        position.moveTo(left, top)
        renderableTopCorner = left to top
    }

    private fun createWarning(list: List<WardrobeSlot>): Pair<String?, List<WardrobeSlot>> {
        var wardrobeWarning: String? = null
        var wardrobeSlots = list

        if (wardrobeSlots.isEmpty()) wardrobeWarning = "§cYour wardrobe is empty :("

        if (config.hideLockedSlots) {
            wardrobeSlots = wardrobeSlots.filter { !it.locked }
            if (wardrobeSlots.isEmpty()) wardrobeWarning = "§cAll your slots are locked? Somehow"
        }

        if (config.hideEmptySlots) {
            wardrobeSlots = wardrobeSlots.filter { !it.isEmpty() }
            if (wardrobeSlots.isEmpty()) wardrobeWarning = "§cAll slots are empty :("
        }
        if (onlyFavorites) {
            wardrobeSlots = wardrobeSlots.filter { it.favorite || it.isCurrentSlot() }
            if (wardrobeSlots.isEmpty()) wardrobeWarning = "§cDidn't set any favorites"
        }

        return wardrobeWarning to wardrobeSlots
    }

    // TODO don't initialize all 18 slots at once, load them lazily when first time hovering over the item.
    private fun createArmorTooltipRenderable(
        slot: WardrobeSlot,
        containerHeight: Int,
        containerWidth: Int,
    ): Renderable {
        val loreList = mutableListOf<Renderable>()
        val hoverableSizes = itemSectionHeights(containerHeight)

        for (armorIndex in 0 until 4) {
            val stack = slot.armor.getOrNull(armorIndex)?.copy()
            var renderable = Renderable.placeholder(containerWidth, hoverableSizes[armorIndex])
            if (stack != null) {
                val toolTip = getToolTip(stack, slot)
                if (toolTip != null) {
                    renderable = Renderable.hoverTips(
                        renderable,
                        tips = toolTip,
                        stack = stack,
                        condition = {
                            !config.showTooltipOnlyKeybind || config.tooltipKeybind.isKeyHeld()
                        },
                        onHover = {
                            if (EstimatedItemValue.config.enabled) EstimatedItemValue.updateItem(stack)
                        },
                    )
                }
            }
            loreList.add(renderable)
        }
        return Renderable.vertical(loreList, spacing = 1)
    }

    private fun getToolTip(stack: SafeItemStack, slot: WardrobeSlot): List<Component>? {
        try {
            // Get tooltip from minecraft and other mods
            val toolTips = stack.getTooltip(Minecraft.getInstance().options.advancedItemTooltips)

            return toolTips
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e,
                "Failed to get tooltip for armor piece in CustomWardrobe",
                "Armor" to stack,
                "Slot" to slot,
                "Lore" to stack.getTooltipCompat(false),
            )
            return null
        }
    }

    // This is needed to keep the background size the same as the slot content size
    protected fun itemSectionHeights(containerHeight: Int): List<Int> {
        val height = containerHeight - 3
        return MutableList(4) { height / 4 }.apply {
            for (k in 0 until height % 4) this[k]++
        }
    }

    private fun createRenderables(): Renderable {
        val (wardrobeWarning, list) = createWarning(api.slots.filter { it.page <= api.maxPage })

        val maxPlayersPerRow = config.maxPlayersPerRow.coerceAtLeast(1)
        val maxPlayersRows = ((MAX_SLOT_PER_PAGE * api.maxPage - 1) / maxPlayersPerRow) + 1
        val containerWidth = loadoutConfig.spacing.slotWidth.scaled()
        val containerHeight = loadoutConfig.spacing.slotHeight.scaled()
        val playerWidth = (containerWidth * (loadoutConfig.spacing.playerScale / 100.0))
        val horizontalSpacing = loadoutConfig.spacing.horizontalSpacing.scaled()
        val verticalSpacing = loadoutConfig.spacing.verticalSpacing.scaled()
        val backgroundPadding = loadoutConfig.spacing.backgroundPadding.scaled()
        val buttonSlotsVerticalSpacing = loadoutConfig.spacing.buttonSlotsVerticalSpacing.scaled()

        var maxRenderableWidth = maxPlayersPerRow * containerWidth + (maxPlayersPerRow - 1) * horizontalSpacing
        var maxRenderableHeight = maxPlayersRows * containerHeight + (maxPlayersRows - 1) * verticalSpacing

        val button = createMenuButtons()

        if (button.width > maxRenderableWidth) maxRenderableWidth = button.width
        maxRenderableHeight += button.height + buttonSlotsVerticalSpacing

        maxRenderableWidth += 2 * backgroundPadding
        maxRenderableHeight += 2 * backgroundPadding
        currentMaxSize = maxRenderableWidth to maxRenderableHeight

        wardrobeWarning?.let { text ->
            val warningRenderable = Renderable.wrappedText(
                text,
                maxRenderableWidth,
                3.0 * activeScale,
                horizontalAlign = HorizontalAlignment.CENTER,
            )
            val withButtons = Renderable.vertical(
                warningRenderable,
                button,
                spacing = buttonSlotsVerticalSpacing,
                horizontalAlign = HorizontalAlignment.CENTER,
            )
            return addGuiBackground(withButtons, backgroundPadding)
        }

        val chunkedList = list.chunked(maxPlayersPerRow)

        val rowsRenderables = chunkedList.map { row ->
            val slotsRenderables = row.map { slot ->
                val armorTooltipRenderable = createArmorTooltipRenderable(slot, containerHeight, containerWidth)
                val (topOutline, bottomOutline) = slot.getOutlineColor()

                val playerBackground = createHoverableRenderable(
                    armorTooltipRenderable,
                    topLayerRenderable = addSlotHoverableButtons(slot),
                    hoveredColor = slot.getSlotColor(),
                    topOutlineColor = topOutline,
                    bottomOutlineColor = bottomOutline,
                    onClick = { slot.clickSlot() },
                )

                val playerRenderable = createSlotContentRenderable(slot, playerWidth, containerHeight, containerWidth)

                Renderable.doubleLayered(
                    playerBackground,
                    playerRenderable,
                    blockBottomHover = false,
                    forceBottomRenderFirst = true
                )
            }
            Renderable.horizontal(slotsRenderables, horizontalSpacing)
        }

        val allSlotsRenderable = Renderable.vertical(
            rowsRenderables,
            verticalSpacing,
            horizontalAlign = HorizontalAlignment.CENTER,
        )

        val withButtons = Renderable.vertical(
            listOf(allSlotsRenderable, button),
            buttonSlotsVerticalSpacing,
            horizontalAlign = HorizontalAlignment.CENTER,
        )

        return addGuiBackground(withButtons, backgroundPadding)
    }

    private fun addGuiBackground(renderable: Renderable, borderPadding: Int) =
        Renderable.drawInsideRoundedRect(
            Renderable.doubleLayered(
                renderable,
                Renderable.clickable(
                    Renderable.text(
                        "§7SkyHanni",
                        horizontalAlign = HorizontalAlignment.RIGHT,
                        verticalAlign = VerticalAlignment.BOTTOM,
                        scale = activeScale,
                    ).let { Renderable.hoverable(hovered = Renderable.underlined(it), unHovered = it) },
                    onLeftClick = {
                        jumpToConfig()
                        onExit()
                    },
                ),
                blockBottomHover = false,
            ),
            loadoutConfig.color.backgroundColor.toColor(),
            padding = borderPadding,
        )

    override fun reset() {
        displayRenderable = null
    }

    override fun onExit() {
        reset()
        api.currentPage = null
    }

    private fun addSlotHoverableButtons(wardrobeSlot: WardrobeSlot): Renderable {
        val textScale = 1.5 * activeScale
        val shouldRender = !wardrobeSlot.isEmpty() && !wardrobeSlot.locked
        if (!shouldRender && !wardrobeSlot.favorite) return Renderable.placeholder(0, 0)
        val list = buildList {
            add(
                Renderable.clickable(
                    Renderable.hoverable(
                        centerString((if (wardrobeSlot.favorite) "§c" else "§7") + "❤", scale = textScale),
                        centerString((if (wardrobeSlot.favorite) "§4" else "§8") + "❤", scale = textScale),
                    ),
                    onLeftClick = {
                        wardrobeSlot.favorite = !wardrobeSlot.favorite
                        update()
                    },
                ),
            )
            if (config.estimatedValue && shouldRender) {
                add(
                    Renderable.hoverTips(
                        centerString("§2$", scale = textScale),
                        api.createPriceLore(wardrobeSlot),
                    ),
                )
            }
        }

        return Renderable.vertical(list, 1, HorizontalAlignment.RIGHT)
    }

    private fun WardrobeSlot.getOutlineColor(): Pair<Color, Color> {
        val (top, bottom) = loadoutConfig.color.topBorderColor.toColor() to loadoutConfig.color.bottomBorderColor.toColor()
        return when {
            isEmpty() || locked -> ColorUtils.TRANSPARENT_COLOR to ColorUtils.TRANSPARENT_COLOR
            !isInCurrentPage() -> top.darker(0.5) to bottom.darker(0.5)
            else -> top to bottom
        }
    }

    // TODO: Make this work outside CustomWardrobe
    fun WardrobeSlot.clickSlot() {
        val previousPageSlot = 45
        val nextPageSlot = 53
        val wardrobePage = api.currentPage ?: return
        if (isInCurrentPage()) {
            if (isEmpty() || locked || waitingForInventoryUpdate) return
            api.currentSlot = if (isCurrentSlot()) null else id
            InventoryUtils.clickSlot(inventorySlot)
        } else {
            if (page < wardrobePage) {
                api.currentPage = wardrobePage - 1
                waitingForInventoryUpdate = true
                InventoryUtils.clickSlot(previousPageSlot)
            } else if (page > wardrobePage) {
                api.currentPage = wardrobePage + 1
                waitingForInventoryUpdate = true
                InventoryUtils.clickSlot(nextPageSlot)
            }
        }
        update()
    }

    private fun WardrobeSlot.getSlotColor(): Color = with(loadoutConfig.color) {
        when {
            isCurrentSlot() -> equippedColor
            favorite && !onlyFavorites -> favoriteColor
            else -> null
        }?.toColor()?.transformIf({ !isInCurrentPage() }) { darker() }
            ?: (if (isInCurrentPage()) samePageColor else otherPageColor).toColor()
                .transformIf({ locked || isEmpty() }) { darker(0.2) }.addAlpha(100)
    }

    private fun isEnabled() = SkyBlockUtils.inSkyBlock && isFeatureEnabled()
}
