package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.inventory.customloadout.CustomLoadoutConfig
import at.hannibal2.skyhanni.utils.ColorUtils.darker
import at.hannibal2.skyhanni.utils.ColorUtils.toColor
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.container.HorizontalContainerRenderable.Companion.horizontal
import at.hannibal2.skyhanni.utils.renderables.container.VerticalContainerRenderable.Companion.vertical
import at.hannibal2.skyhanni.utils.renderables.primitives.placeholder
import at.hannibal2.skyhanni.utils.renderables.primitives.text
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.world.inventory.ChestMenu
import java.awt.Color
import kotlin.math.min

/**
 * A custom gui that replaces a skyblock menu. It is shown by [CustomMenuScreen],
 * while the edit mode ([CustomMenuEditScreen]) shows the original skyblock menu instead.
 */
@Suppress("TooManyFunctions")
abstract class AbstractCustomMenu(internal val guiName: String) {

    protected val loadoutConfig: CustomLoadoutConfig get() = SkyHanniMod.feature.inventory.customLoadout

    internal var switchingScreens = false
    val inCustomMenu get() = (MinecraftCompat.screen as? CustomMenuScreen)?.gui === this
    val editMode get() = (MinecraftCompat.screen as? CustomMenuEditScreen)?.gui === this

    protected var activeScale: Double = 1.0
    protected var currentMaxSize: Pair<Int, Int>? = null
    protected var lastScreenSize: Pair<Int, Int>? = null

    abstract var onlyFavorites: Boolean

    /** The area of the screen that is covered by the gui, or null to use the whole screen. */
    open val rectangle: ScreenRectangle? get() = null

    // Called by MenuScreensHook
    internal abstract fun shouldReplace(inventoryName: String): Boolean

    abstract fun renderOverlay(screenWidth: Int, screenHeight: Int)

    internal abstract fun updateScreenSize(gui: Pair<Int, Int>): Boolean

    internal abstract fun onInventoryUpdate()

    internal abstract fun handleKeybinds()

    internal abstract fun update()

    internal abstract fun reset()

    protected open fun onExit() = reset()

    open fun shouldShowItemList(): Boolean = false

    internal open fun onScreenClosed() = Unit

    protected fun checkScreenSize(screenSize: Pair<Int, Int>): Boolean {
        if (screenSize == lastScreenSize) return false
        lastScreenSize = screenSize
        return updateScreenSize(screenSize)
    }

    protected fun fitScale(gui: Pair<Int, Int>, maxSize: Pair<Int, Int>): Boolean {
        val previousActiveScale = activeScale
        val autoScaleWidth = 0.95 * gui.first / (maxSize.first / activeScale)
        val autoScaleHeight = 0.95 * gui.second / (maxSize.second / activeScale)
        activeScale = (loadoutConfig.spacing.globalScale / 100.0).coerceAtMost(min(autoScaleWidth, autoScaleHeight))
        return activeScale != previousActiveScale
    }

    protected fun Int.scaled() = (this * activeScale).toInt()

    protected fun createMenuButtons(): Renderable {
        val backButton = createLabeledButton("§aBack") {
            InventoryUtils.clickSlot(48)
            onExit()
        }
        val closeButton = createLabeledButton("§cClose") {
            InventoryUtils.clickSlot(49)
            onExit()
        }
        val favoriteButton = createLabeledButton(
            "§eFavorite",
            hoveredColor = if (onlyFavorites) Color(85, 255, 85, 200) else Color(255, 85, 85, 200),
        ) {
            onlyFavorites = !onlyFavorites
            update()
        }
        val editButton = createLabeledButton("§bEdit") {
            DelayedRun.runNextTick {
                reset()
                enterEditMode()
            }
        }

        val row = Renderable.horizontal(
            listOf(backButton, closeButton, favoriteButton),
            loadoutConfig.spacing.buttonHorizontalSpacing.scaled(),
            horizontalAlign = HorizontalAlignment.CENTER,
        )
        return Renderable.vertical(
            listOf(row, editButton),
            loadoutConfig.spacing.buttonVerticalSpacing.scaled(),
            horizontalAlign = HorizontalAlignment.CENTER,
            verticalAlign = VerticalAlignment.CENTER,
        )
    }

    fun createLabeledButton(
        text: String,
        hoveredColor: Color = Color(130, 130, 130, 200),
        unhoveredColor: Color = hoveredColor.darker(0.57),
        onClick: () -> Unit,
    ): Renderable {
        val buttonWidth = loadoutConfig.spacing.buttonWidth.scaled()
        val buttonHeight = loadoutConfig.spacing.buttonHeight.scaled()

        return Renderable.hoverable(
            Renderable.drawInsideRoundedRectWithOutline(
                Renderable.doubleLayered(
                    Renderable.clickable(Renderable.placeholder(buttonWidth, buttonHeight), onClick),
                    centerString(text, scale = activeScale),
                    false,
                ),
                hoveredColor,
                padding = 0,
                topOutlineColor = loadoutConfig.color.topBorderColor.toColor().rgb,
                bottomOutlineColor = loadoutConfig.color.bottomBorderColor.toColor().rgb,
                borderOutlineThickness = 2,
                horizontalAlign = HorizontalAlignment.CENTER,
            ),
            Renderable.drawInsideRoundedRect(
                Renderable.doubleLayered(
                    Renderable.placeholder(buttonWidth, buttonHeight),
                    centerString(text, scale = activeScale),
                ),
                unhoveredColor,
                padding = 0,
                horizontalAlign = HorizontalAlignment.CENTER,
            ),
        )
    }

    protected fun createHoverableRenderable(
        hoveredRenderable: Renderable,
        topLayerRenderable: Renderable = Renderable.placeholder(0, 0),
        hoveredColor: Color,
        topOutlineColor: Color,
        bottomOutlineColor: Color,
        onClick: () -> Unit,
        onRightClick: () -> Unit = {},
    ): Renderable = Renderable.hoverable(
        Renderable.drawInsideRoundedRectWithOutline(
            Renderable.doubleLayered(
                Renderable.clickable(
                    hoveredRenderable,
                    mapOf(KeyboardManager.LEFT_MOUSE to onClick, KeyboardManager.RIGHT_MOUSE to onRightClick),
                ),
                topLayerRenderable,
            ),
            hoveredColor,
            padding = 0,
            topOutlineColor = topOutlineColor.rgb,
            bottomOutlineColor = bottomOutlineColor.rgb,
            borderOutlineThickness = loadoutConfig.spacing.outlineThickness,
            blur = loadoutConfig.spacing.outlineBlur,
            horizontalAlign = HorizontalAlignment.CENTER,
            verticalAlign = VerticalAlignment.CENTER,
        ),
        Renderable.drawInsideRoundedRect(
            Renderable.placeholder(hoveredRenderable.width, hoveredRenderable.height),
            hoveredColor,
            padding = 0,
            horizontalAlign = HorizontalAlignment.CENTER,
            verticalAlign = VerticalAlignment.CENTER,
        ),
    )

    protected fun centerString(
        text: String,
        scale: Double = 1.0,
        color: Color = Color.WHITE,
    ) = Renderable.text(text, scale, color, horizontalAlign = HorizontalAlignment.CENTER)

    fun enterEditMode() {
        val screen = MinecraftCompat.screen as? CustomMenuScreen ?: return
        val player = MinecraftCompat.localPlayerOrNull ?: return

        switchingScreens = true
        MinecraftCompat.screen = CustomMenuEditScreen(
            screen.menu,
            player.inventory,
            screen.title,
            this,
        )
    }

    fun exitEditMode() {
        val screen = MinecraftCompat.screen as? CustomMenuEditScreen ?: return
        val player = MinecraftCompat.localPlayerOrNull ?: return
        val handler = player.containerMenu as? ChestMenu ?: return

        switchingScreens = true
        MinecraftCompat.screen = CustomMenuScreen(
            handler,
            screen.title,
            this,
        )
    }
}
