package at.hannibal2.skyhanni.test.framework.fixtures

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.SkyHanniConfig
import at.hannibal2.skyhanni.test.framework.TestFixture
import org.junit.jupiter.api.extension.ExtensionContext

class ConfigFixture : TestFixture {
    private lateinit var previous: SkyHanniConfig

    val config: SkyHanniConfig get() = SkyHanniMod.feature

    override fun setUp(context: ExtensionContext) {
        previous = SkyHanniMod.feature
        SkyHanniMod.feature = SkyHanniConfig()
    }

    override fun tearDown(context: ExtensionContext) {
        SkyHanniMod.feature = previous
    }
}
