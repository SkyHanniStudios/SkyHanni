package at.hannibal2.skyhanni.test.vector

import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import at.hannibal2.skyhanni.utils.vector.VectorFormat.formatRounded
import at.hannibal2.skyhanni.utils.vector.VectorFormat.toChatFormat
import at.hannibal2.skyhanni.utils.vector.VectorFormat.toLocalFormat
import at.hannibal2.skyhanni.utils.vector.VectorMath.blockCenter
import at.hannibal2.skyhanni.utils.vector.VectorMath.distanceChebyshevIgnoreY
import at.hannibal2.skyhanni.utils.vector.VectorMath.distanceIgnoreY
import at.hannibal2.skyhanni.utils.vector.VectorMath.floor
import net.minecraft.world.phys.Vec3
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SkyHanniVectorTest {

    @Test
    fun `negative zero equals positive zero`() {
        val negative = SkyHanniVector(-0.0, -0.0, -0.0)
        assertEquals(SkyHanniVector.ZERO, negative)
        assertEquals(SkyHanniVector.ZERO.hashCode(), negative.hashCode())
        assertTrue(setOf(SkyHanniVector.ZERO).contains(negative))
    }

    @Test
    fun `equals a plain Vec3 with the same components in both directions`() {
        assertEquals(SkyHanniVector(1, 2, 3), Vec3(1.0, 2.0, 3.0))
        assertEquals(Vec3(1.0, 2.0, 3.0), SkyHanniVector(1, 2, 3))
        assertNotEquals(SkyHanniVector(1, 2, 3), Vec3(1.0, 2.0, 4.0))
    }

    @Test
    fun `cross product is right handed`() {
        assertEquals(SkyHanniVector(0, 0, 1), SkyHanniVector(Vec3.X_AXIS).cross(Vec3.Y_AXIS))
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
        assertEquals(SkyHanniVector(1.0, -3.0, 3.0), v.floor())
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
