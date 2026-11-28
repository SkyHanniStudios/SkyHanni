package at.hannibal2.skyhanni.test.framework.base

import at.hannibal2.skyhanni.test.framework.CapturesOutput
import at.hannibal2.skyhanni.test.framework.NeedsNeuRepo
import at.hannibal2.skyhanni.test.framework.fixtures.NeuRepoFixture
import at.hannibal2.skyhanni.test.framework.fixtures.OutputFixture
import org.junit.jupiter.api.BeforeEach

@NeedsNeuRepo
@CapturesOutput
abstract class ItemTest : MinecraftTest() {
    protected lateinit var neuRepo: NeuRepoFixture
        private set
    protected lateinit var output: OutputFixture
        private set

    @BeforeEach
    fun bindItemFixtures(neuRepo: NeuRepoFixture, output: OutputFixture) {
        this.neuRepo = neuRepo
        this.output = output
    }
}
