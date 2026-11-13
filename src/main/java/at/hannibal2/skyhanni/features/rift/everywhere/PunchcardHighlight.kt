package at.hannibal2.skyhanni.features.rift.everywhere

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.MobData
import at.hannibal2.skyhanni.data.model.SkyblockStat
import at.hannibal2.skyhanni.events.IslandJoinEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.entity.EntityClickEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.AutoUpdatingItemStack
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColorUtils.toColor
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.EntityUtils.isNpc
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.container.HorizontalContainerRenderable.Companion.horizontal
import at.hannibal2.skyhanni.utils.renderables.primitives.ItemStackRenderable.Companion.item
import at.hannibal2.skyhanni.utils.renderables.primitives.text
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.world.entity.LivingEntity
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PunchcardHighlight {
    private val config get() = SkyHanniMod.feature.rift.punchcard
    private var lastRiftServer: String = ""

    private var listening = false

    private val patternGroup = RepoPattern.group("rift.punchcard")

    /**
     * REGEX-TEST: PUNCHCARD! You punched [MVP+] ThorQOM and both regained +25 Rift Time!
     * REGEX-TEST: PUNCHCARD! You punched Metafighter and both regained +25 Rift Time!
     * REGEX-TEST: PUNCHCARD! You punched [VIP] RickyLafleur22 and both regained +25 Rift Time!
     */
    @Suppress("MaxLineLength")
    private val punchedPattern by patternGroup.pattern(
        "new.colorless",
        "PUNCHCARD! You punched (?:.*?)?(?<name>\\w+) and both regained \\+25${SkyblockStat.RIFT_TIME.hypixelIcon} Rift Time!",
    )

    /**
     * REGEX-TEST: AWKWARD! This player has already been punched by you... somehow!
     */
    private val repeatPattern by patternGroup.pattern(
        "repeat.colorless",
        "AWKWARD! This player has already been punched by you\\.\\.\\. somehow!",
    )

    /**
     * REGEX-TEST: UH OH! You reached the limit of 20 players you can punch in one session!
     */
    private val limitPattern by patternGroup.pattern(
        "limit.colorless",
        "UH OH! You reached the limit of 20 players you can punch in one session!",
    )

    private val playerList: MutableSet<String> = mutableSetOf()
    private val playerQueue = mutableListOf<String>()

    private val PUNCHCARD_ARTIFACT = "PUNCHCARD_ARTIFACT".toInternalName()
    private val displayIcon by AutoUpdatingItemStack(PUNCHCARD_ARTIFACT)
    private var display: Renderable = Renderable.text("hello")

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    private fun onPlayerSpawn(event: MobEvent.Spawn.Player) {
        if (!config.highlight.get()) return
        if (config.reverse.get()) return
        val size = playerList.size
        if (size >= 20) return
        val entity = event.mob
        if (!playerList.contains(entity.name)) {
            colorPlayer(entity.baseEntity)
        }
    }

    @HandleEvent
    private fun onConfigLoad() {
        ConditionalUtils.onToggle(
            config.highlight,
            config.color,
            config.reverse,
        ) {
            reloadColors()
        }
        ConditionalUtils.onToggle(
            config.gui,
            config.compact,
            config.reverseGUI,
        ) {
            display = drawDisplay()
        }
        ConditionalUtils.onToggle(
            config.highlight,
            config.color,
        ) {
            checkPunchcard()
        }
    }

    private var warningCooldown = SimpleTimeMark.farPast()

    private fun checkPunchcard() {
        if (!RiftApi.inRift()) return

        val hasPunchcard = InventoryUtils.isItemInInventory(PUNCHCARD_ARTIFACT)
        if (!hasPunchcard && warningCooldown.passedSince() > 30.seconds) {
            warningCooldown = SimpleTimeMark.now()
            ChatUtils.chat("You don't seem to own a Punchcard Artifact, this feature will not work without one.")
        }
    }

    @HandleEvent
    private fun onIslandJoin(event: IslandJoinEvent) {
        DelayedRun.runDelayed(1500.milliseconds) {
            if (playerList.isEmpty()) return@runDelayed
            if (event.island != IslandType.THE_RIFT) return@runDelayed

            if (HypixelData.server.isNotEmpty() && lastRiftServer != HypixelData.server) {
                reloadColors()
                lastRiftServer = HypixelData.server
                playerList.clear()
            }
            display = drawDisplay()
        }
    }

    private fun colorPlayer(entity: LivingEntity) {
        val color = config.color.get().toColor()
        RenderLivingEntityHelper.setEntityColor(entity, color) { IslandType.THE_RIFT.isInIsland() }
    }

    private fun removePlayerColor(entity: LivingEntity) {
        RenderLivingEntityHelper.removeEntityColor(entity)
    }

    private fun onResetCommand() {
        playerList.clear()
        playerQueue.clear()
        if (config.reverse.get()) {
            MobData.players.forEach {
                colorPlayer(it.baseEntity)
            }
        } else {
            MobData.players.forEach {
                removePlayerColor(it.baseEntity)
            }
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    private fun onEntityClick(event: EntityClickEvent) {
        val entity = event.clickedEntity
        if (entity !is AbstractClientPlayer) return
        if (entity.isNpc()) return
        val name = entity.name.string
        if (name in playerList || name in playerQueue) return
        playerQueue.add(name)
        listening = true
        DelayedRun.runDelayed(1.seconds) {
            if (name in playerQueue) playerQueue.remove(name)
            listening = false
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    private fun onChat(event: SkyHanniChatEvent.Allow) {
        if (!listening) return
        if (playerQueue.isEmpty()) return
        val message = event.cleanMessage
        val queuedName = playerQueue[0]
        punchedPattern.matchMatcher(message) {
            val name = group("name")
            addPunch(name, queuedName)
            return
        }
        if (limitPattern.matches(message) || repeatPattern.matches(message)) addPunch(queuedName)
    }

    private fun addPunch(playerName: String, queuedName: String = playerName) {
        playerList.add(playerName)
        playerQueue.remove(queuedName)
        val player = MobData.players.firstOrNull { it.name == playerName } ?: return
        if (!config.reverse.get()) removePlayerColor(player.baseEntity)
        else colorPlayer(player.baseEntity)
        display = drawDisplay()
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    private fun onGuiRenderOverlay() {
        if (!config.gui.get()) return

        config.position.renderRenderable(display, "Punchcard Overlay")
    }

    private fun drawDisplay(): Renderable {
        var string = ""
        if (!config.compact.get()) string += "Punchcard Artifact: "
        string += "§d" + if (!config.reverseGUI.get()) playerList.size
        else 20 - playerList.size

        return Renderable.horizontal(
            Renderable.item(displayIcon),
            Renderable.text(string),
            spacing = 1,
        )
    }

    private fun reloadColors() {
        MobData.players.forEach {
            removePlayerColor(it.baseEntity)
        }
        if (!config.highlight.get()) return
        val reverse = config.reverse.get()
        for (player in MobData.players.filter { (reverse && it.name in playerList) || (!reverse && it.name !in playerList) }) {
            colorPlayer(player.baseEntity)
        }
    }

    @HandleEvent
    private fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.registerBrigadier("shresetpunchcard") {
            description = "Resets the Rift Punchcard Artifact player list."
            category = CommandCategory.USERS_RESET
            simpleCallback { onResetCommand() }
        }
    }
}
