package at.hannibal2.skyhanni.test.framework.base

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.api.event.SkyHanniEvents
import at.hannibal2.skyhanni.test.framework.CapturesOutput
import at.hannibal2.skyhanni.test.framework.NeedsClock
import at.hannibal2.skyhanni.test.framework.NeedsIsland
import at.hannibal2.skyhanni.test.framework.fixtures.ClockFixture
import at.hannibal2.skyhanni.test.framework.fixtures.LocationFixture
import at.hannibal2.skyhanni.test.framework.fixtures.OutputFixture
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

@NeedsIsland
@NeedsClock
@CapturesOutput
abstract class EventTest : ConfigTest() {
    protected lateinit var location: LocationFixture
        private set
    protected lateinit var clock: ClockFixture
        private set
    protected lateinit var output: OutputFixture
        private set

    private val registered = mutableListOf<Any>()

    @BeforeEach
    fun bindEventFixtures(location: LocationFixture, clock: ClockFixture, output: OutputFixture) {
        this.location = location
        this.clock = clock
        this.output = output
    }

    protected fun register(vararg modules: Any) = modules.forEach {
        SkyHanniEvents.register(it)
        registered.add(it)
    }

    protected fun <T : SkyHanniEvent> post(event: T): T = event.also { it.post() }

    @AfterEach
    fun unregisterModules() {
        registered.forEach(SkyHanniEvents::unregister)
        registered.clear()
    }
}
