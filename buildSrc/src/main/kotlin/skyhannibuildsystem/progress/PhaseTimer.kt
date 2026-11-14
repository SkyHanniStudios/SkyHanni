package skyhannibuildsystem.progress

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

class PhaseTimer {

    private val phases = LinkedHashMap<String, Duration>()
    private var phase: String? = null
    private val start = TimeSource.Monotonic.markNow()
    private var phaseStart = start

    fun observe(status: String) {
        val next = status.takeWhile { !it.isDigit() }.trim()
        if (next != phase) enter(next)
    }

    fun stop(): String {
        enter(null)
        return buildList {
            add("total ${start.elapsedNow().seconds()}")
            phases.mapTo(this) { (name, duration) -> "$name ${duration.seconds()}" }
        }.joinToString(" | ")
    }

    private fun enter(next: String?) {
        val now = TimeSource.Monotonic.markNow()
        phase?.let { phases.merge(it, now - phaseStart, Duration::plus) }
        phase = next
        phaseStart = now
    }

    private fun Duration.seconds() = toString(DurationUnit.SECONDS, decimals = 3)
}
