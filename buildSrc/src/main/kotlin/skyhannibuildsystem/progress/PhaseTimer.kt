package skyhannibuildsystem.progress

import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

class PhaseTimer {

    private val phases = LinkedHashMap<String, Duration>()
    private var phase: String? = null
    private var phaseStart = 0L
    private val start = System.nanoTime()

    fun observe(status: String) {
        val next = status.takeWhile { !it.isDigit() }.trim()
        if (next != phase) enter(next)
    }

    fun stop(): String {
        enter(null)
        return buildList {
            add("total ${(System.nanoTime() - start).nanoseconds.seconds()}")
            phases.mapTo(this) { (name, duration) -> "$name ${duration.seconds()}" }
        }.joinToString(" | ")
    }

    private fun enter(next: String?) {
        val now = System.nanoTime()
        phase?.let { phases.merge(it, (now - phaseStart).nanoseconds, Duration::plus) }
        phase = next
        phaseStart = now
    }

    private fun Duration.seconds() = toString(DurationUnit.SECONDS, decimals = 3)
}
