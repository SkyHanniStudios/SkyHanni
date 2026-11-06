package at.hannibal2.skyhanni.test.framework.fixtures

import at.hannibal2.skyhanni.test.framework.TestFixture
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.TimeProvider
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.time.Duration

class ClockFixture : TestFixture {
    var nowMillis: Long = START_MILLIS
        private set

    val now: SimpleTimeMark get() = SimpleTimeMark(nowMillis)

    fun advance(duration: Duration) {
        nowMillis += duration.inWholeMilliseconds
    }

    fun set(mark: SimpleTimeMark) {
        nowMillis = mark.toMillis()
    }

    override fun setUp(context: ExtensionContext) {
        nowMillis = START_MILLIS
        SimpleTimeMark.timeProvider = TimeProvider { nowMillis }
    }

    override fun tearDown(context: ExtensionContext) = SimpleTimeMark.resetTimeProvider()

    private companion object {
        const val START_MILLIS = 1_700_000_000_000L
    }
}
