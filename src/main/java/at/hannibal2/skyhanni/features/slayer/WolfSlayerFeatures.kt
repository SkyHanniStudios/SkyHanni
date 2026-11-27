package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.SlayerApi
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.data.mob.Mob.Companion.belongsToPlayer
import at.hannibal2.skyhanni.data.title.TitleManager
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.ParticleEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColorUtils.toColor
import at.hannibal2.skyhanni.utils.EntityUtils.canBeSeen
import at.hannibal2.skyhanni.utils.EntityUtils.getEntitiesNearby
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SkyBlockUtils
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.compat.formattedTextCompat
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.render.WorldRenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.render.WorldRenderUtils.drawLineToCrosshair
import at.hannibal2.skyhanni.utils.render.WorldRenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.primitives.text
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object WolfSlayerFeatures {

    private val config get() = SlayerApi.config.wolf

    private val patternGroup = RepoPattern.group("slayer.wolf")

    /**
     * REGEX-TEST: ☠ Packmaster Soul
     */
    private val bossNamePattern by patternGroup.pattern(
        "boss-name",
        "(?:☠ )?Packmaster Soul",
    )

    /**
     * REGEX-TEST: ☠ Warrior Wolf
     * REGEX-TEST: ☠ Ranger Wolf
     * REGEX-TEST: ☠ Howling Wolf
     * REGEX-TEST: ☠ Explosive Wolf
     */
    private val minionNamePattern by patternGroup.pattern(
        "minion-name",
        "(?:☠ )?(?:Warrior|Ranger|Howling|Explosive) Wolf",
    )

    /** Boss and minion mobs whose owner stand has not been seen yet. */
    private val pending = mutableSetOf<Mob>()
    private var boss: Mob? = null
    private val minions = mutableSetOf<Mob>()

    /** The entity to hit, cached per tick since the damage indicator asks for it every frame. */
    private var target: Entity? = null

    /** The wolf the boss hides in, only ever found through hints the player can see or hear. */
    var possessed: Mob? = null
        private set
    private var wasInvisible = false
    private var hiddenTicks = 0
    private val lastPositions = mutableMapOf<Mob, LorenzVec>()
    private val fastTicks = mutableMapOf<Mob, Int>()
    private var bloodfrenzyDisplay: Display.TextDisplay? = null
    private var bloodfrenzyAlerted = false

    private fun isEnabled() = boss != null && (config.highlight || config.drawLine)

    @HandleEvent(onlyOnSkyblock = true)
    private fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        val name = event.mob.name
        if (bossNamePattern.matches(name) || minionNamePattern.matches(name)) pending += event.mob
    }

    // minions are kept when only the boss despawns, so a re-created boss Mob finds its wolves again
    @HandleEvent
    private fun onMobDespawn(event: MobEvent.DeSpawn.SkyblockMob) {
        pending -= event.mob
        minions -= event.mob
        lastPositions -= event.mob
        fastTicks -= event.mob
        if (boss === event.mob) resetFight()
    }

    @HandleEvent
    private fun onWorldChange() {
        pending.clear()
        minions.clear()
        resetFight()
    }

    private fun resetFight() {
        boss = null
        target = null
        possessed = null
        wasInvisible = false
        hiddenTicks = 0
        lastPositions.clear()
        fastTicks.clear()
        bloodfrenzyDisplay = null
        bloodfrenzyAlerted = false
    }

    @HandleEvent(onlyOnSkyblock = true)
    private fun onSecondPassed() {
        pending.removeIf { mob ->
            mob.findOwner()
            if (mob.owner == null) return@removeIf false
            if (mob.belongsToPlayer()) {
                if (bossNamePattern.matches(mob.name)) boss = mob else minions += mob
            }
            true
        }
        (minions + listOfNotNull(boss)).forEach { mob ->
            mob.highlight(config.highlightColor.toColor()) { config.highlight && target === mob.baseEntity }
        }
        val stand = boss?.armorStand?.getLorenzVec()
        bloodfrenzyDisplay = stand?.getEntitiesNearby<Display.TextDisplay>(1.5)
            ?.filter { it.y > stand.y }
            ?.minByOrNull { it.getLorenzVec().distance(stand) }
    }

    private fun checkBloodfrenzyAlert() {
        if (!config.bloodfrenzyAlert) return
        val stacks = bloodfrenzyDisplay?.text?.string?.split(' ', '/')?.firstNotNullOfOrNull { it.toIntOrNull() } ?: 0
        if (stacks < config.bloodfrenzyAlertStacks) {
            bloodfrenzyAlerted = false
            return
        }
        if (bloodfrenzyAlerted) return
        bloodfrenzyAlerted = true
        TitleManager.sendTitle("§c§lRIGHT CLICK! §7($stacks Bloodfrenzy)", duration = 2.seconds)
        ChatUtils.chat("§cYour boss has §f$stacks §cBloodfrenzy stacks, right click to remove them!")
        SoundUtils.playBeepSound()
    }

    @HandleEvent
    private fun onTick() {
        checkBloodfrenzyAlert()
        if (isEnabled()) detectByMovement()
        target = findTarget()
    }

    private fun detectByMovement() {
        val hidden = boss?.baseEntity?.isInvisible == true
        hiddenTicks = if (hidden) hiddenTicks + 1 else 0
        for (wolf in minions) {
            val position = wolf.baseEntity.getLorenzVec()
            val speed = lastPositions.put(wolf, position)?.distanceIgnoreY(position) ?: 0.0
            val fast = if (hidden && speed > 0.22) fastTicks.getOrDefault(wolf, 0) + 1 else 0
            fastTicks[wolf] = fast
            if (possessed == null && hiddenTicks > 5 && fast >= 2) possessed = wolf
        }
    }

    private val wolfRepellentSounds = mapOf(
        "item.trident.riptide_1" to 1.7936507f,
        "entity.generic.extinguish_fire" to 1.5873016f,
    )

    @HandleEvent(onlyOnSkyblock = true)
    private fun onPlaySound(event: PlaySoundEvent) {
        if (config.muteWolfRepellent && wolfRepellentSounds[event.soundName] == event.pitch) event.cancel()
    }

    @HandleEvent(onlyOnSkyblock = true)
    private fun onParticle(event: ParticleEvent) {
        if (!isEnabled() || event.type != ParticleTypes.SOUL || event.count != 2) return
        possessed = minions.firstOrNull { it.baseEntity.getLorenzVec().up(0.5).distance(event.location) <= 1.0 } ?: return
    }

    private fun findTarget(): Entity? {
        val bossEntity = boss?.baseEntity ?: return null
        if (!bossEntity.isInvisible) {
            if (wasInvisible) possessed = null
            wasInvisible = false
            return bossEntity
        }
        wasInvisible = true
        return possessed?.takeIf { it in minions }?.baseEntity
    }

    fun isPossessing(entity: Entity) = entity === boss?.baseEntity && entity.isInvisible

    @HandleEvent
    private fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        val target = target?.takeIf { it.canBeSeen(30.0) } ?: return
        val location = event.exactLocation(target)
        if (config.highlight && target !== boss?.baseEntity) {
            event.drawDynamicText(location.up(target.bbHeight + 0.5), "§d§lPOSSESSED", 1.0)
        }
        if (config.drawLine) {
            event.drawLineToCrosshair(location.up(0.5), config.lineColor, config.lineWidth, true)
        }
    }

    @HandleEvent
    private fun onGuiRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (boss == null || !config.bloodfrenzyDisplay) return
        val text = bloodfrenzyDisplay?.text?.formattedTextCompat()?.takeIf { it.isNotBlank() } ?: return
        val line = if (text.contains("Bloodfrenzy")) text else "§cBloodfrenzy: §f$text"
        config.bloodfrenzyPosition.renderRenderable(Renderable.text(line), posLabel = "Bloodfrenzy")
    }
}
