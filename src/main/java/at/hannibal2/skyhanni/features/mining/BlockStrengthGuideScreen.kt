package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.data.model.SkyblockStat
import at.hannibal2.skyhanni.features.mining.BlockStrengthGuide.DisplayOres
import at.hannibal2.skyhanni.features.mining.BlockStrengthGuide.LoadState
import at.hannibal2.skyhanni.features.mining.BlockStrengthGuide.SpeedClass
import at.hannibal2.skyhanni.utils.AbstractCustomMenuScreen
import at.hannibal2.skyhanni.utils.ConditionalUtils.transformIf
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.fractionOf
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.SafeItemStack
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.StringUtils.insert
import at.hannibal2.skyhanni.utils.StringUtils.pluralize
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.TimeUtils.ticks
import at.hannibal2.skyhanni.utils.collection.CollectionUtils.distribute
import at.hannibal2.skyhanni.utils.collection.RenderableCollectionUtils.addString
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.renderAndScale
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.renderXYAligned
import at.hannibal2.skyhanni.utils.renderables.container.HorizontalContainerRenderable.Companion.horizontal
import at.hannibal2.skyhanni.utils.renderables.container.VerticalContainerRenderable.Companion.vertical
import at.hannibal2.skyhanni.utils.renderables.container.table.TableRenderable.Companion.table
import at.hannibal2.skyhanni.utils.renderables.primitives.ItemStackRenderable.Companion.item
import at.hannibal2.skyhanni.utils.renderables.primitives.WrappedStringRenderable.Companion.wrappedText
import at.hannibal2.skyhanni.utils.renderables.primitives.placeholder
import at.hannibal2.skyhanni.utils.renderables.primitives.text
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import java.awt.Color
import kotlin.math.ceil

/** Replaces the stats menu opened by [BlockStrengthGuide] and draws the guide over the open container. */
class BlockStrengthGuideScreen(menu: ChestMenu, title: Component) : AbstractCustomMenuScreen(menu, title) {

    private val instantMineColor = Color(0x1E, 0x90, 0xFF)
    private val softCapColor = Color(0x00, 0xFA, 0x9A)
    private val baseColor = Color(0xFF, 0x63, 0x37)
    private val outlineColor = Color(0, 0, 0, 200).rgb
    private val speedIcon get() = SkyblockStat.MINING_SPEED.icon

    private val loading = Renderable.text(
        "Loading...",
        scale = 2.0,
        horizontalAlign = HorizontalAlignment.CENTER,
        verticalAlign = VerticalAlignment.CENTER,
    )
    private val speedLabels = listOf("Base", "Gemstone", "Metal").map {
        Renderable.text(it, scale = 0.75, horizontalAlign = HorizontalAlignment.CENTER)
    }

    private var guide: Renderable? = null
    private var showExtraInfos = KeyboardManager.isModifierKeyDown()
    private var showingStatsMenu = false

    override fun isSwitchingScreens(): Boolean = showingStatsMenu

    override fun slotChanged(container: AbstractContainerMenu, slotId: Int, stack: SafeItemStack) = Unit

    override fun onDrawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        when (BlockStrengthGuide.pollStats()) {
            LoadState.LOADING -> loading.renderXYAligned(0, 0, width, height)
            LoadState.READY -> drawGuide(mouseX, mouseY)
            LoadState.FAILED -> showStatsMenu()
        }
    }

    private fun drawGuide(mouseX: Int, mouseY: Int) {
        val modifierDown = KeyboardManager.isModifierKeyDown()
        if (showExtraInfos != modifierDown) {
            showExtraInfos = modifierDown
            guide = null
        }
        val current = guide ?: buildGuide().also { guide = it }
        Renderable.withMousePosition(mouseX, mouseY) {
            current.renderAndScale(0, 0, width, height, 20)
        }
    }

    private fun showStatsMenu() {
        if (showingStatsMenu) return
        showingStatsMenu = true
        DelayedRun.runNextTick {
            val player = MinecraftCompat.localPlayerOrNull ?: return@runNextTick
            if (MinecraftCompat.screen === this) MinecraftCompat.screen = ContainerScreen(getMenu(), player.inventory, title)
        }
    }

    private fun buildGuide(): Renderable {
        val speed = BlockStrengthGuide.requestSpeed()
        return Renderable.drawInsideRoundedRectWithOutline(
            Renderable.vertical(
                Renderable.vertical(buildHeader(speed), horizontalAlign = HorizontalAlignment.CENTER),
                Renderable.table(DisplayOres.entries.map { it.row(speed) }.distribute(3), 5, 3),
                spacing = 8,
            ),
            color = LorenzColor.GRAY.addOpacity(180),
            topOutlineColor = outlineColor,
            bottomOutlineColor = outlineColor,
            borderOutlineThickness = 3,
            verticalAlign = VerticalAlignment.CENTER,
            horizontalAlign = HorizontalAlignment.CENTER,
        )
    }

    private fun buildHeader(speed: SpeedClass): List<Renderable> = listOf(
        Renderable.text(SkyblockStat.MINING_SPEED.iconWithName, horizontalAlign = HorizontalAlignment.CENTER),
        Renderable.horizontal(
            Renderable.table(listOf(speedLabels, speed.labels()), xSpacing = 5),
            Renderable.clickable(
                Renderable.text(
                    "§${if (BlockStrengthGuide.inMineshaft) 'b' else '7'}Mineshaft",
                    scale = 0.5,
                    verticalAlign = VerticalAlignment.CENTER,
                ),
                onLeftClick = {
                    BlockStrengthGuide.inMineshaft = !BlockStrengthGuide.inMineshaft
                    guide = null
                },
            ),
            spacing = 3,
        ),
    )

    private fun SpeedClass.labels() = listOf(base, gemstone, metal).map {
        Renderable.text("§6${it.toInt().addSeparators()}", horizontalAlign = HorizontalAlignment.CENTER)
    }

    private fun SpeedClass.forOre(ore: OreBlock) = base + when (ore.category) {
        OreCategory.DWARVEN_METAL -> metal
        OreCategory.GEMSTONE -> gemstone
        OreCategory.ORE -> this.ore
        OreCategory.BLOCK -> block
    }

    private fun DisplayOres.row(speeds: SpeedClass): Renderable {
        val ore = oreBlocks.first()
        val speed = speeds.forOre(ore)
        val ticks = ore.miningTicks(speed)
        val (progressBar, percentLine, untilNextLine) = progress(ticks, speed, ore)
        return Renderable.hoverTips(
            Renderable.horizontal(Renderable.item(icon), progressBar, Renderable.text("$ticks"), spacing = 0),
            tips = buildTips(ore, speed.toInt().addSeparators(), ticks, percentLine, untilNextLine),
        )
    }

    private fun progress(ticks: Int, speed: Double, ore: OreBlock): Triple<Renderable, String, String?> = when (ticks) {
        1 -> Triple(Renderable.progressBar(1.0, instantMineColor, instantMineColor, width = 100), "§6Instant Mine", null)
        4 -> {
            val fraction = speed.fractionOf(ore.speedForInstantMine)
            Triple(
                Renderable.progressBar(fraction, softCapColor, instantMineColor, width = 100),
                "§a${fraction.times(100).roundTo(1)}% §fto Instant Mine",
                "${missing(ore.speedForInstantMine - speed)} §b1 §ftick",
            )
        }

        else -> {
            val fraction = speed.fractionOf(ore.speedSoftCap)
            val next = ticks - 1
            Triple(
                Renderable.progressBar(fraction, baseColor, softCapColor, width = 100),
                "§a${fraction.times(100).roundTo(1)}% §fto Soft Cap",
                "${missing(ore.speedNeededForNextTick(speed))} §b$next §f${"tick".pluralize(next)}",
            )
        }
    }

    private fun missing(speed: Double) = "§6${ceil(speed).toInt().addSeparators()} $speedIcon §cmissing §fto"

    private fun DisplayOres.buildTips(
        ore: OreBlock,
        speed: String,
        ticks: Int,
        percentLine: String,
        untilNextLine: String?,
    ): List<Renderable> = buildList {
        val blockName = name.allLettersFirstUppercase()
        val time = ticks.ticks.format(showMilliSeconds = true).transformIf({ ticks % 2 == 1 }) { insert(length - 1, '5') }
        val softCap = ore.speedSoftCap.addSeparators()
        val instant = ore.speedForInstantMine.addSeparators()
        val gap = Renderable.placeholder(0, 5)

        addString(blockName)
        add(gap)
        addInfo(
            "§3Ticks: §f$ticks §7(§b$time§7)",
            "It takes you §b$time§7 to break $blockName.",
            "Only correct if the server has 20 TPS.",
        )
        addInfo("§3Your: §6$speed $speedIcon", "You have §6$speed mining speed", "when breaking $blockName :)")
        untilNextLine?.let {
            addInfo(it, "The mining speed you need more", "to mine $blockName in §b${ticks - 1}")
        }
        addString(percentLine)
        add(gap)
        addInfo(
            "§3Block Strength: §f${ore.strength.addSeparators()}",
            "This defines the \"toughness\" of a block.",
            "A higher number means it takes longer",
            "to break $blockName.",
        )
        addInfo(
            "§3Softcap: §6$softCap $speedIcon",
            "Having more than §6$softCap mining speed",
            "will §c§lNOT §r§7break $blockName any faster.",
        )
        addInfo(
            "§3Instant: §6$instant $speedIcon",
            "Once you reach §6$instant mining speed",
            "you break $blockName in §e§lone click§7.",
        )
        add(gap)
        addString("§3Category: §f${ore.category.toString().allLettersFirstUppercase()}")
        addString("§3Blocks in that group:")
        add(Renderable.wrappedText(hoverText, setWidth = 200))
        if (!showExtraInfos) {
            add(gap)
            addString("§eHold ${KeyboardManager.getModifierKeyName()} key to show extra infos!")
        }
    }

    private fun MutableList<Renderable>.addInfo(line: String, vararg extraInfos: String) {
        addString(line)
        if (showExtraInfos) extraInfos.forEach { addString("  §7$it") }
    }
}
