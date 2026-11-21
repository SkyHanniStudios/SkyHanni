package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.SkyHanniMod.launchRepeating
import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.SkyBlockUtils
import at.hannibal2.skyhanni.utils.coroutines.CoroutineSettings
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@PrimaryFunction("onSecondPassed")
class SecondPassedEvent(private val totalSeconds: Int) : SkyHanniEvent() {
    fun repeatSeconds(i: Int) = totalSeconds % i == 0

    @SkyHanniModule
    companion object {
        private val timerManagerSettings = CoroutineSettings("fixed rate timer manager")
        private var totalSeconds = 0

        init {
            timerManagerSettings.launchRepeating(1.seconds, initialDelay = Duration.ZERO) {
                DelayedRun.runOrNextTick {
                    if (!SkyBlockUtils.onHypixel) return@runOrNextTick
                    SecondPassedEvent(totalSeconds).post()
                    totalSeconds++
                }
            }
        }
    }
}
