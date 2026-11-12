package at.hannibal2.skyhanni.features.combat.mobs

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.SlayerApi
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.render.WorldRenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object AreaMiniBossFeatures {

    private val config get() = SkyHanniMod.feature.combat.mobs
    private var lastSpawnTime = SimpleTimeMark.farPast()
    private var miniBossType: AreaMiniBossType? = null
    private var respawnCooldown = 11.seconds
    val currentMobs = mutableSetOf<Mob>()

    @HandleEvent
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        val type = AreaMiniBossType.entries.find { it.displayName == event.mob.name } ?: return
        miniBossType = type
        val time = SimpleTimeMark.now()
        val diff = time - lastSpawnTime
        if (diff in 5.seconds..20.seconds) {
            respawnCooldown = diff
        }
        lastSpawnTime = time
        if (config.areaBossHighlight) {
            event.mob.highlight(type.color.addOpacity(type.colorTransparency).toChromaColor())
        }
        currentMobs.add(event.mob)
    }

    @HandleEvent
    fun onMobDespawn(event: MobEvent.DeSpawn.SkyblockMob) {
        currentMobs.remove(event.mob)
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!SlayerApi.isInAnyArea) return
        if (!config.areaBossRespawnTimer) return

        val miniBoss = miniBossType ?: return

        val time = miniBoss.getTime()
        miniBoss.spawnLocations.filter { it.distanceToPlayer() < 15 }
            .forEach { event.drawDynamicText(it, time, 1.2, seeThroughBlocks = false) }
    }

    private fun AreaMiniBossType.getTime(): String {
        val spawnedSince = lastSpawnTime.passedSince()
        if (respawnCooldown <= spawnedSince) return "§c?"

        val estimatedTime = respawnCooldown - spawnedSince
        val format = estimatedTime.format(showMilliSeconds = true)
        return color.getChatColor() + format
    }

    @HandleEvent
    fun onWorldChange() {
        miniBossType = null
    }

    // TODO move to repo
    private enum class AreaMiniBossType(
        val displayName: String,
        val color: LorenzColor,
        val colorTransparency: Int,
        vararg val spawnLocations: SkyHanniVector,
    ) {
        GOLDEN_GHOUL(
            "Golden Ghoul", LorenzColor.YELLOW, 127,
            SkyHanniVector(-99, 39, -86),
            SkyHanniVector(-128, 42, -138),
        ),
        OLD_WOLF(
            "Old Wolf", LorenzColor.GOLD, 60,
            SkyHanniVector(-248, 123, 54),
            SkyHanniVector(-256, 105, 75),
            SkyHanniVector(-268, 90, 97),
            SkyHanniVector(-258, 94, 75),
            SkyHanniVector(-225, 92, 127),
        ),
        SOUL_OF_THE_ALPHA(
            "Soul of the Alpha", LorenzColor.GOLD, 60,
            SkyHanniVector(-381, 56, -94),
            SkyHanniVector(-394, 63, -52),
            SkyHanniVector(-386, 50, -2),
            SkyHanniVector(-396, 58, 29),
        ),
        VOIDLING_EXTREMIST(
            "Voidling Extremist", LorenzColor.LIGHT_PURPLE, 127,
            SkyHanniVector(-591, 10, -304),
            SkyHanniVector(-544, 21, -301),
            SkyHanniVector(-593, 26, -328),
            SkyHanniVector(-565, 41, -307),
            SkyHanniVector(-573, 51, -353),
        ),
        MILLENNIA_AGED_BLAZE(
            "Millennia-Aged Blaze", LorenzColor.DARK_RED, 60,
            SkyHanniVector(-292, 97, -999),
            SkyHanniVector(-232, 77, -951),
            SkyHanniVector(-304, 73, -952),
            SkyHanniVector(-281, 82, -1010),
            SkyHanniVector(-342, 86, -1035),
        ),
    }
}
