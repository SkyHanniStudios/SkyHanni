package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.garden.pests.PestKillEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorOpenEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.garden.tracker.CropFeverTracker
import at.hannibal2.skyhanni.features.garden.tracker.GardenBpsTracker
import at.hannibal2.skyhanni.features.garden.tracker.PestProfitTracker
import at.hannibal2.skyhanni.features.garden.tracker.RareCropTracker
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.Stopwatch
import at.hannibal2.skyhanni.utils.tracker.GardenSession
import at.hannibal2.skyhanni.utils.tracker.SessionUptime
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenUptimeManager {
    private val config get() = GardenApi.config.trackerUptimeSettings
    private val trackerSet: Set<SkyHanniTracker<*, *>> = setOf(
        RareCropTracker.tracker,
        CropFeverTracker,
        PestProfitTracker,
        GardenBpsTracker.tracker,
    )
    private val afkTracker = Stopwatch()

    @HandleEvent
    private fun onConfigLoad() {
        ConditionalUtils.onToggle(config.types) {
            modify { it.update() }
        }
    }

    @HandleEvent
    private fun onWorldChange() {
        modify { it.pauseSessionUptime() }
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    private fun onTick(event: SkyHanniTickEvent) {
        if (!event.isMod(5)) return
        if (!afkTracker.isPaused()) {
            modify { it.update() }
        }
        if ((afkTracker.getLapTime() ?: return) >= config.afkTimeout.seconds) {
            modify { it.pauseSessionUptime() }
            afkTracker.pause()
        }
    }

    @HandleEvent
    private fun onCropClick() {
        // we do not want this tracker to be greedy, and exclude visitor/pest downtime whenever possible
        modify { it.swapActiveSession(SessionUptime.Garden(GardenSession.CROP), false) }
        afkTracker.start(true)
    }

    @HandleEvent
    private fun onPestKill() {
        modify { it.swapActiveSession(SessionUptime.Garden(GardenSession.PEST)) }
        afkTracker.start(true)
    }

    @HandleEvent
    private fun onVisitorOpen() {
        modify { it.swapActiveSession(SessionUptime.Garden(GardenSession.VISITOR)) }
        afkTracker.start(true)
    }

    fun modify(modifyFunction: (SkyHanniTracker<*, *>) -> Unit) {
        trackerSet.forEach(modifyFunction)
    }
}
