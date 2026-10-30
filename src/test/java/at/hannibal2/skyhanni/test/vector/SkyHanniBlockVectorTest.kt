package at.hannibal2.skyhanni.test.vector

import at.hannibal2.skyhanni.utils.vector.SkyHanniBlockVector
import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SkyHanniBlockVectorTest {

    @Test
    fun `packing keeps coordinates at the world limits`() {
        listOf(
            SkyHanniBlockVector(-541, 94, 227),
            SkyHanniBlockVector(30_000_000, 2047, -30_000_000),
            SkyHanniBlockVector(-30_000_000, -2048, 30_000_000),
        ).forEach { v ->
            assertEquals(v, SkyHanniBlockVector.fromPacked(v.asLong()))
            assertEquals(v, SkyHanniBlockVector(v.x, v.y, v.z))
        }
    }

    @Test
    fun `negative offsets carry across packed fields`() {
        assertEquals(SkyHanniBlockVector(-1, -1, -1), SkyHanniBlockVector.ZERO.offset(x = -1, y = -1, z = -1))
    }

    @Test
    fun `distanceSq does not overflow`() {
        val far = SkyHanniBlockVector(30_000_000, 0, 30_000_000)
        assertEquals(1_800_000_000_000_000L, SkyHanniBlockVector.ZERO.distanceSq(far))
    }

    @Test
    fun `center is the middle of the block`() {
        assertEquals(SkyHanniVector(1.5, -1.5, 3.5), SkyHanniBlockVector(1, -2, 3).center())
    }
}
