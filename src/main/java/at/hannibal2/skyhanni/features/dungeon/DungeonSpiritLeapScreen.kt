package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.config.features.dungeon.spiritleap.SpiritLeapColorConfig
import at.hannibal2.skyhanni.features.dungeon.DungeonSpiritLeapOverlay.PlayerStackInfo
import at.hannibal2.skyhanni.utils.AbstractCustomMenuScreen
import at.hannibal2.skyhanni.utils.ColorUtils.toColor
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SafeItemStack
import at.hannibal2.skyhanni.utils.StringUtils.cleanPlayerName
import at.hannibal2.skyhanni.utils.compat.formattedTextCompatLeadingWhiteLessResets
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.container.HorizontalContainerRenderable.Companion.horizontal
import at.hannibal2.skyhanni.utils.renderables.container.VerticalContainerRenderable.Companion.vertical
import at.hannibal2.skyhanni.utils.renderables.container.table.TableRenderable.Companion.table
import at.hannibal2.skyhanni.utils.renderables.primitives.ItemStackRenderable.Companion.item
import at.hannibal2.skyhanni.utils.renderables.primitives.WrappedStringRenderable.Companion.wrappedText
import at.hannibal2.skyhanni.utils.renderables.primitives.placeholder
import at.hannibal2.skyhanni.utils.renderables.primitives.text
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

/** Replaces the spirit leap menu matched by [DungeonSpiritLeapOverlay] and draws the leap buttons over the open container. */
class DungeonSpiritLeapScreen(menu: ChestMenu, title: Component) : AbstractCustomMenuScreen(menu, title) {

    private val config get() = DungeonSpiritLeapOverlay.config
    private val colorConfig get() = config.colorConfig
    private val keybinds get() = DungeonSpiritLeapOverlay.spiritLeapKeybinds
    private val scaleFactor get() = min(width, height).toDouble() / max(width, height).toDouble()

    private var players = emptyList<PlayerStackInfo>()

    override fun slotChanged(container: AbstractContainerMenu, slotId: Int, stack: SafeItemStack) = Unit

    override fun onDrawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        players = readPlayers()
        val display = buildDisplay(players)
        Position((width - display.width) / 2, (height - display.height) / 2)
            .renderRenderable(display, posLabel = "Spirit Leap Overlay", addToGuiManager = false)
    }

    fun leap(index: Int) {
        players.getOrNull(index)?.let(DungeonSpiritLeapOverlay::leapToPlayer)
    }

    private fun readPlayers(): List<PlayerStackInfo> = buildList {
        for ((slot, stack) in getMenu().getUpperItems()) {
            if (stack.getLore().isEmpty()) continue
            val name = stack.hoverName.formattedTextCompatLeadingWhiteLessResets().cleanPlayerName()
            add(PlayerStackInfo(DungeonApi.getPlayerInfo(name), stack, slot.index))
        }
    }.sortedBy { it.playerInfo?.dungeonClass?.ordinal }

    private fun buildDisplay(players: List<PlayerStackInfo>): Renderable {
        val buttons = players.mapIndexedNotNull { index, info -> info.playerInfo?.let { buildButton(index, info, it) } }
        if (buttons.isEmpty()) return Renderable.wrappedText(
            setWidth = (width * 0.8).toInt(),
            text = "No targets available for leap.",
            scale = scaleFactor * 3,
        )
        return Renderable.table(
            buttons.take(4).chunked(2),
            xSpacing = 18,
            ySpacing = 18,
            horizontalAlign = HorizontalAlignment.CENTER,
            verticalAlign = VerticalAlignment.CENTER,
        )
    }

    private fun buildButton(index: Int, info: PlayerStackInfo, member: DungeonApi.TeamMember): Renderable = Renderable.clickable(
        render = Renderable.drawInsideRoundedRectWithOutline(
            input = Renderable.vertical(
                buildKeybindHint(index),
                Renderable.placeholder(0, height = (-15 * scaleFactor).toInt()),
                Renderable.fixedSizeColumn(buildRow(info.stack, member), height = (height * 0.35).toInt()),
            ),
            verticalAlign = VerticalAlignment.CENTER,
            color = member.backgroundColor(),
            topOutlineColor = 0xFFFFF,
            bottomOutlineColor = 0xFFFFF,
            borderOutlineThickness = 2,
            radius = 7,
            smoothness = 10,
            padding = 5,
        ),
        onLeftClick = { DungeonSpiritLeapOverlay.leapToPlayer(info) },
    )

    private fun buildRow(stack: SafeItemStack, member: DungeonApi.TeamMember): Renderable {
        val gap = Renderable.placeholder((width * 0.01).toInt(), 0)
        val icon = Renderable.drawInsideRoundedRect(
            Renderable.item(stack) { scale = scaleFactor * 0.9 + 2.7 },
            color = Color(255, 255, 255, 100),
            radius = 5,
        )
        return Renderable.fixedSizeLine(
            Renderable.horizontal(gap, icon, gap, buildPlayerInfo(member), verticalAlign = VerticalAlignment.CENTER),
            width = (width * 0.40).toInt(),
            verticalAlign = VerticalAlignment.CENTER,
        )
    }

    private fun buildPlayerInfo(member: DungeonApi.TeamMember): Renderable {
        val textWidth = (width * 0.25).toInt()
        return Renderable.vertical(
            Renderable.wrappedText(member.username, setWidth = textWidth, scale = scaleFactor + 1.5),
            Renderable.placeholder(0, (height * 0.03).toInt()),
            Renderable.wrappedText(member.classInfo(), setWidth = textWidth, scale = scaleFactor * 0.9 + 1.1),
            horizontalAlign = HorizontalAlignment.CENTER,
            verticalAlign = VerticalAlignment.CENTER,
        )
    }

    private fun buildKeybindHint(index: Int): Renderable {
        val showHint = config.spiritLeapKeybindConfig.showKeybindHint && index in keybinds.indices
        if (!showHint) return Renderable.placeholder(width = 10, height = (12 * scaleFactor).toInt())
        return Renderable.drawInsideRoundedRectOutline(
            Renderable.text(
                KeyboardManager.getKeyName(keybinds[index]),
                scaleFactor * 0.9 + 0.7,
                verticalAlign = VerticalAlignment.CENTER,
            ),
            topOutlineColor = 0xFFFFF,
            bottomOutlineColor = 0xFFFFF,
            borderOutlineThickness = 2,
            padding = 4,
            horizontalAlign = HorizontalAlignment.RIGHT,
        )
    }

    private fun DungeonApi.TeamMember.classInfo(): String = buildString {
        dungeonClass?.let {
            append(it.displayName)
            if (config.showDungeonClassLevel) append(" $classLevel")
            if (playerDead) append(" (Dead)")
        }
    }

    private fun DungeonApi.TeamMember.backgroundColor(): Color = when {
        playerDead -> colorConfig.deadTeammateColor
        else -> when (dungeonClass) {
            DungeonApi.DungeonClass.ARCHER -> colorConfig.archerClassColor
            DungeonApi.DungeonClass.MAGE -> colorConfig.mageClassColor
            DungeonApi.DungeonClass.BERSERK -> colorConfig.berserkClassColor
            DungeonApi.DungeonClass.TANK -> colorConfig.tankClassColor
            DungeonApi.DungeonClass.HEALER -> colorConfig.healerClassColor
            else -> SpiritLeapColorConfig.defaultColor
        }
    }.toColor()
}
