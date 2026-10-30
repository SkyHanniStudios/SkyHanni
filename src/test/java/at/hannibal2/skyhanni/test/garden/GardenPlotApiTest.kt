package at.hannibal2.skyhanni.test.garden

import at.hannibal2.skyhanni.features.garden.plot.GardenPlotApi
import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GardenPlotApiTest {

    @Test
    fun `plot edges prefer the positive plot`() {
        mapOf(
            SkyHanniVector(0.0, 10.0, -48.0) to 0,
            SkyHanniVector(-48.0, 10.0, 0.0) to 0,
            SkyHanniVector(48.0, 10.0, 0.0) to 3,
            SkyHanniVector(0.0, 10.0, 48.0) to 4,
            SkyHanniVector(48.0, 10.0, 48.0) to 8,
        ).forEach { (location, expectedPlotId) ->
            assertEquals(expectedPlotId, GardenPlotApi.getPlot(location)?.id, "location $location")
        }
    }
}
