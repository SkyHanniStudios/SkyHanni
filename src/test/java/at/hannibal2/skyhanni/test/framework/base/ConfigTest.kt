package at.hannibal2.skyhanni.test.framework.base

import at.hannibal2.skyhanni.config.SkyHanniConfig
import at.hannibal2.skyhanni.test.framework.NeedsConfig
import at.hannibal2.skyhanni.test.framework.fixtures.ConfigFixture
import org.junit.jupiter.api.BeforeEach

@NeedsConfig
abstract class ConfigTest : MinecraftTest() {
    private lateinit var configFixture: ConfigFixture

    protected val config: SkyHanniConfig get() = configFixture.config

    @BeforeEach
    fun bindConfigFixture(configFixture: ConfigFixture) {
        this.configFixture = configFixture
    }
}
