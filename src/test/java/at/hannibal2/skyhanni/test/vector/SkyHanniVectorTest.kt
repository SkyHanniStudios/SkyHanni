package at.hannibal2.skyhanni.test.vector

import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import at.hannibal2.skyhanni.utils.vector.VectorBlocks.blockCenter
import at.hannibal2.skyhanni.utils.vector.VectorBlocks.floorToBlock
import at.hannibal2.skyhanni.utils.vector.VectorFormat.formatRounded
import at.hannibal2.skyhanni.utils.vector.VectorFormat.toChatFormat
import at.hannibal2.skyhanni.utils.vector.VectorFormat.toLocalFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.PI

class SkyHanniVectorTest {

    @Test
    fun `negative zero equals positive zero`() {
        val negative = SkyHanniVector(-0.0, -0.0, -0.0)
        assertEquals(SkyHanniVector.ZERO, negative)
        assertEquals(SkyHanniVector.ZERO.hashCode(), negative.hashCode())
    }

    @Test
    fun `cross product is right handed`() {
        assertEquals(SkyHanniVector.Z_AXIS, SkyHanniVector.X_AXIS cross SkyHanniVector.Y_AXIS)
    }

    @Test
    fun `rotateAroundY turns x into negative z`() {
        assertEquals(SkyHanniVector(0.0, 0.0, -1.0), SkyHanniVector.X_AXIS.rotateAroundY(PI / 2).roundTo(6))
    }

    @Test
    fun `ignore y distances drop the y axis`() {
        val point = SkyHanniVector(3.0, 10.0, 4.0)
        assertEquals(5.0, SkyHanniVector.ZERO.distanceIgnoreY(point))
        assertEquals(4.0, SkyHanniVector.ZERO.distanceChebyshevIgnoreY(point))
    }

    @Test
    fun `block helpers floor toward negative infinity`() {
        val v = SkyHanniVector(1.7, -2.3, 3.0)
        assertEquals(SkyHanniVector(1.0, -3.0, 3.0), v.floorToBlock().toVector())
        assertEquals(SkyHanniVector(1.5, -2.5, 3.5), v.blockCenter())
    }

    @Test
    fun `formats match LorenzVec output`() {
        val v = SkyHanniVector(-91.74, 70.0, 29.26)
        assertEquals("x: -91, y: 70, z: 29", v.toChatFormat())
        assertEquals("-91 70 29", v.toLocalFormat())
        assertEquals("-91.7 70.0 29.3", v.formatRounded(10))
    }
}
