package at.hannibal2.skyhanni.test.framework.fixtures

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.test.SkyBlockIslandTest
import at.hannibal2.skyhanni.test.framework.NeedsIsland
import at.hannibal2.skyhanni.test.framework.TestFixture
import at.hannibal2.skyhanni.utils.SkyBlockUtils
import org.junit.jupiter.api.extension.ExtensionContext

class LocationFixture : TestFixture {

    fun enter(island: IslandType) {
        SkyBlockUtils.testInSkyBlock = true
        SkyBlockIslandTest.testIsland = island
    }

    fun leaveSkyBlock() {
        SkyBlockUtils.testInSkyBlock = false
        SkyBlockIslandTest.testIsland = null
    }

    override fun setUp(context: ExtensionContext) {
        val island = context.requiredTestClass.getAnnotation(NeedsIsland::class.java)?.island ?: IslandType.HUB
        enter(island)
    }

    override fun tearDown(context: ExtensionContext) {
        SkyBlockUtils.testInSkyBlock = null
        SkyBlockIslandTest.testIsland = null
    }
}
