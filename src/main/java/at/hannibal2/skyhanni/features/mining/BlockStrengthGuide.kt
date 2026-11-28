package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.hotx.HotmData
import at.hannibal2.skyhanni.data.hotx.HotmReward
import at.hannibal2.skyhanni.data.model.SkyblockStat
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonApi
import at.hannibal2.skyhanni.features.inventory.CurrentEquipmentApi
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraApi
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SafeItemStack
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getHypixelEnchantments
import at.hannibal2.skyhanni.utils.SkyBlockUtils
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.compat.BlockCompat
import at.hannibal2.skyhanni.utils.compat.ColoredBlockCompat
import net.minecraft.world.level.block.Blocks
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object BlockStrengthGuide {

    internal enum class DisplayOres(private val iconDel: () -> SafeItemStack, val oreBlocks: Set<OreBlock>) {
        VANILLA_ORES(
            { SafeItemStack(Blocks.REDSTONE_BLOCK) },
            setOf(
                OreBlock.COAL_ORE,
                OreBlock.IRON_ORE,
                OreBlock.LAPIS_ORE,
                OreBlock.GOLD_ORE,
                OreBlock.EMERALD_ORE,
                OreBlock.DIAMOND_ORE,
                OreBlock.QUARTZ_ORE,
                OreBlock.REDSTONE_ORE,
            ),
        ),
        PURE_ORES(
            { SafeItemStack(Blocks.GOLD_BLOCK) },
            setOf(
                OreBlock.PURE_COAL,
                OreBlock.PURE_IRON,
                OreBlock.PURE_LAPIS,
                OreBlock.PURE_GOLD,
                OreBlock.PURE_EMERALD,
                OreBlock.PURE_DIAMOND,
                OreBlock.PURE_QUARTZ,
                OreBlock.PURE_REDSTONE,
            ),
        ),
        TITANIUM(
            { BlockCompat.createSmoothDiorite() },
            setOf(OreBlock.HIGH_TIER_MITHRIL),
        ),
        GRAY_MITHRIL(
            { ColoredBlockCompat.GRAY.createWoolStack() },
            setOf(OreBlock.LOW_TIER_MITHRIL),
        ),
        GREEN_MITHRIL(
            { SafeItemStack(Blocks.PRISMARINE) },
            setOf(OreBlock.MID_TIER_MITHRIL),
        ),
        BLUE_MITHRIL(
            { ColoredBlockCompat.LIGHT_BLUE.createWoolStack() },
            setOf(OreBlock.HIGH_TIER_MITHRIL),
        ),
        TUNGSTEN_UMBER(
            { SafeItemStack(Blocks.CLAY) },
            setOf(
                OreBlock.LOW_TIER_UMBER,
                OreBlock.MID_TIER_UMBER,
                OreBlock.HIGH_TIER_UMBER,
                OreBlock.LOW_TIER_TUNGSTEN_MINESHAFT,
                OreBlock.LOW_TIER_TUNGSTEN_MINESHAFT,
                OreBlock.HIGH_TIER_TUNGSTEN,
            ),
        ),
        GLACITE(
            { SafeItemStack(Blocks.PACKED_ICE) },
            setOf(OreBlock.GLACITE),
        ),
        OBSIDIAN(
            { SafeItemStack(Blocks.OBSIDIAN) },
            setOf(OreBlock.OBSIDIAN),
        ),
        RUBY(
            { ColoredBlockCompat.RED.createGlassStack() },
            setOf(OreBlock.RUBY),
        ),
        NUCLEUS_GEMSTONES(
            { ColoredBlockCompat.LIGHT_BLUE.createGlassStack() },
            setOf(OreBlock.AMBER, OreBlock.AMETHYST, OreBlock.JADE, OreBlock.SAPPHIRE),
        ),
        OPAL(
            { ColoredBlockCompat.WHITE.createGlassStack() },
            setOf(OreBlock.OPAL),
        ),
        TOPAZ(
            { ColoredBlockCompat.YELLOW.createGlassStack() },
            setOf(OreBlock.TOPAZ),
        ),
        JASPER(
            { ColoredBlockCompat.MAGENTA.createGlassStack() },
            setOf(OreBlock.JASPER),
        ),
        TUNNEL_GEMSTONES(
            { ColoredBlockCompat.BLACK.createGlassStack() },
            setOf(OreBlock.ONYX, OreBlock.PERIDOT, OreBlock.CITRINE, OreBlock.AQUAMARINE),
        ),
        HARD_STONE(
            { SafeItemStack(Blocks.STONE) },
            setOf(OreBlock.HARD_STONE_HOLLOWS, OreBlock.HARD_STONE_TUNNELS, OreBlock.HARD_STONE_MINESHAFT),
        ),
        COBBLE_STONE(
            { SafeItemStack(Blocks.COBBLESTONE) },
            setOf(OreBlock.COBBLESTONE),
        ),
        STONE(
            { SafeItemStack(Blocks.STONE) },
            setOf(OreBlock.STONE),
        ),
        SULPHUR(
            { SafeItemStack(Blocks.SPONGE) },
            setOf(OreBlock.SULPHUR),
        ),
        NETHERRACK(
            { SafeItemStack(Blocks.NETHERRACK) },
            setOf(OreBlock.NETHERRACK),
        ),
        END_STONE(
            { SafeItemStack(Blocks.END_STONE) },
            setOf(OreBlock.END_STONE),
        ),
        ;

        val icon by lazy(this.iconDel)

        val hoverText get() = oreBlocks.joinToString(", ") { it.name.allLettersFirstUppercase() }
    }

    internal var inMineshaft = false

    // TODO Dwarven Equip (Needs a Equipment API) , Goblin Pet and Mithril Pet (need the PetAPI v2)
    internal fun requestSpeed(): SpeedClass {
        val itemInHand = InventoryUtils.getItemInHand()
        return SpeedClass(
            base = (
                SkyblockStat.MINING_SPEED.lastKnownValue ?: 0.0
                ) + if (inMineshaft) HotmData.EAGER_ADVENTURER.getReward()[HotmReward.MINING_SPEED] ?: 0.0 else 0.0,
            metal = HotmData.STRONG_ARM.getReward()[HotmReward.MINING_SPEED] ?: 0.0,
            gemstone = (
                HotmData.PROFESSIONAL.getReward()[HotmReward.MINING_SPEED] ?: 0.0
                ) + (
                itemInHand?.getHypixelEnchantments()?.get("lapidary")?.times(20.0) ?: 0.0
                ) + when (itemInHand?.getInternalNameOrNull()?.asString()) {
                "GEMSTONE_DRILL_1", "GEMSTONE_DRILL_2", "GEMSTONE_DRILL_3", "GEMSTONE_DRILL_4" -> 800.0
                else -> 0.0
            },
            ore = 0.0,
            block = 0.0,
        )
    }

    internal data class SpeedClass(
        val base: Double,
        val metal: Double,
        val gemstone: Double,
        val ore: Double,
        val block: Double,
    )

    private var shouldBlockSHMenu = false
        set(value) {
            field = value
            if (value) lastSet = SimpleTimeMark.now()
        }

    private var statsOpened = false

    private var lastSet = SimpleTimeMark.farPast()
    private var lastRunCommand = SimpleTimeMark.farPast()

    private var waitingForStats = false // if trying to fetch the stats actively
    private var statsMenuOpened = false // /stats GUI is visible on the user's screen

    fun onCommand() {
        if (!SkyBlockUtils.inSkyBlock) {
            ChatUtils.userError("Join SkyBlock to use the Block Strength Guide!")
            return
        }
        when {
            RiftApi.inRift() -> "in the Rift"
            DungeonApi.inDungeon() -> "in Dungeons"
            KuudraApi.inKuudra -> "in Kuudra"
            else -> null
        }?.let {
            ChatUtils.userError("The Block Strength Guide does not work $it!")
            return

        }
        lastRunCommand = SimpleTimeMark.now()
        shouldBlockSHMenu = true
        statsOpened = false
        waitingForStats = true
        statsMenuOpened = false
        HypixelCommands.stats()
    }

    internal enum class LoadState {
        LOADING,
        READY,
        FAILED,
    }

    internal fun shouldReplace(inventoryName: String): Boolean =
        shouldBlockSHMenu && waitingForStats && lastRunCommand.passedSince() < 3.seconds &&
            CurrentEquipmentApi.equipmentAndStatsMenuPattern.matches(inventoryName)

    internal fun pollStats(): LoadState {
        if (!waitingForStats) return if (statsOpened) LoadState.READY else LoadState.FAILED
        if (statsMenuOpened && SkyblockStat.MINING_SPEED.lastAssignment.passedSince() < 1.0.seconds) {
            statsOpened = true
            waitingForStats = false
            return LoadState.READY
        }
        if (lastRunCommand.passedSince() < 2.seconds) return LoadState.LOADING
        shouldBlockSHMenu = false
        waitingForStats = false
        ErrorManager.logErrorStateWithData(
            "could not load mining data for /shblockstrength command",
            "opened /stats and found no mining speed in the next 2s",
            "island" to SkyBlockUtils.currentIsland,
            "graph area" to SkyBlockUtils.graphArea,
            "scoreboard area" to SkyBlockUtils.scoreboardArea,
            "location" to LocationUtils.playerLocation(),
            betaOnly = true,
        )
        return LoadState.FAILED
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (!CurrentEquipmentApi.inventory.isInside()) return
        if (waitingForStats && lastRunCommand.passedSince() < 3.seconds) {
            statsMenuOpened = true
        }
        DelayedRun.runDelayed(100.milliseconds) {
            if (lastRunCommand.passedSince() < 3.seconds && !waitingForStats) {
                lastRunCommand = SimpleTimeMark.farPast()
            }
        }
    }

    @HandleEvent
    fun onInventoryClose() {
        if (!statsOpened) return
        shouldBlockSHMenu = false
    }

    @HandleEvent
    fun onWorldChange() {
        shouldBlockSHMenu = false
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.registerBrigadier("shblockstrength") {
            description = "Shows how many ticks you need to break any block with your mining speed."
            category = CommandCategory.MAIN
            aliases = listOf("shminingspeed")
            simpleCallback { onCommand() }
        }
    }
}
