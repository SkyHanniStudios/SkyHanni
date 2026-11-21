package at.hannibal2.skyhanni.test.vector

import at.hannibal2.skyhanni.utils.vector.KVector
import at.hannibal2.skyhanni.utils.vector.VectorConversions
import at.hannibal2.skyhanni.utils.vector.VectorConversions.toKVector
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.PI

class KVectorTest {

    @Test
    fun `negative zero equals positive zero`() {
        val negative = KVector(-0.0, -0.0, -0.0)
        assertEquals(KVector.ZERO, negative)
        assertEquals(KVector.ZERO.hashCode(), negative.hashCode())
        assertTrue(setOf(KVector.ZERO).contains(negative))
    }

    @Test
    fun `equals a plain Vec3 with the same components in both directions`() {
        assertEquals(KVector(1, 2, 3), Vec3(1.0, 2.0, 3.0))
        assertEquals(Vec3(1.0, 2.0, 3.0), KVector(1, 2, 3))
        assertNotEquals(KVector(1, 2, 3), Vec3(1.0, 2.0, 4.0))
    }

    @Test
    fun `equals block positions and joml vectors with the same components`() {
        assertEquals(KVector(1, 2, 3), BlockPos(1, 2, 3))
        assertEquals(KVector(1, 2, 3), Vector3f(1f, 2f, 3f))
        assertEquals(KVector(1, 2, 3), KVector(BlockPos(1, 2, 3)))
        assertEquals(KVector(1, 2, 3), KVector(Vector3f(1f, 2f, 3f)))
        assertNotEquals(KVector(1.5, 2, 3), BlockPos(1, 2, 3))
    }

    @Test
    fun `math on a KVector stays a KVector`() {
        val v = KVector(3, 0, 4)
        assertEquals(KVector::class, v.add(1.0, 1.0, 1.0)::class)
        assertEquals(KVector::class, v.scale(2.0)::class)
        assertEquals(KVector::class, v.normalize()::class)
        assertEquals(KVector::class, v.lerp(Vec3.ZERO, 0.5)::class)
        assertEquals(KVector::class, v.cross(Vec3.ZERO)::class)
    }

    @Test
    fun `operators accept a plain Vec3 operand`() {
        val plain = Vec3(1.0, 2.0, 3.0)
        val v = KVector(1, 2, 3)
        assertEquals(KVector(2, 4, 6), v + plain)
        assertEquals(KVector.ZERO, v - plain)
        assertEquals(KVector(1, 4, 9), v * plain)
        assertEquals(KVector(2, 4, 6), v * 2)
        assertEquals(KVector(-1, -2, -3), -v)
    }

    @Test
    fun `cross product is right handed`() {
        assertEquals(KVector(0, 0, 1), KVector(Vec3.X_AXIS).cross(Vec3.Y_AXIS))
    }

    @Test
    fun `rotateAroundY turns x into negative z`() {
        assertEquals(KVector(0.0, 0.0, -1.0), KVector(Vec3.X_AXIS).rotateAroundY(PI / 2).roundTo(6))
    }

    @Test
    fun `ignore y distances drop the y axis`() {
        val point = Vec3(3.0, 10.0, 4.0)
        assertEquals(5.0, KVector.ZERO.distanceIgnoreY(point))
        assertEquals(4.0, KVector.ZERO.distanceChebyshevIgnoreY(point))
    }

    @Test
    fun `block helpers floor toward negative infinity`() {
        val v = KVector(1.7, -2.3, 3.0)
        assertEquals(KVector(1.0, -3.0, 3.0), v.floor())
        assertEquals(KVector(1.5, -2.5, 3.5), v.blockCenter())
    }

    @Test
    fun `normalize divides by the length like LorenzVec`() {
        assertEquals(KVector(0.6, 0.0, 0.8), KVector(3, 0, 4).normalize())
        assertTrue(KVector.ZERO.normalize().x.isNaN())
    }

    @Test
    fun `interpolate rejects a factor outside zero to one`() {
        val v = KVector(0, 0, 0)
        assertEquals(KVector(1, 1, 1), v.interpolate(KVector(2, 2, 2), 0.5))
        assertThrows(IllegalArgumentException::class.java) { v.interpolate(KVector(2, 2, 2), 1.5) }
    }

    @Test
    fun `a collection of three doubles becomes a vector`() {
        assertEquals(KVector(1, 2, 3), listOf(1.0, 2.0, 3.0).toKVector())
        assertEquals(KVector(1, 2, 3), linkedSetOf(1.0, 2.0, 3.0).toKVector())
        assertThrows(IllegalArgumentException::class.java) { listOf(1.0, 2.0).toKVector() }
    }

    @Test
    fun `parseStoredOrNull returns null for anything but three numbers`() {
        assertEquals(KVector(1.5, -2, 3), VectorConversions.parseStoredOrNull("1.5: -2 :3"))
        assertNull(VectorConversions.parseStoredOrNull("1:2"))
        assertNull(VectorConversions.parseStoredOrNull("1:2:x"))
    }

    @Test
    fun `parseStoredList reads quoted json lines`() {
        val text = "\"-625:119:-962\",\n\"-626:121:-971\""
        assertEquals(listOf(KVector(-625, 119, -962), KVector(-626, 121, -971)), VectorConversions.parseStoredList(text))
    }

    @Test
    fun `formats match LorenzVec output`() {
        val v = KVector(-91.74, 70.0, 29.26)
        assertEquals("x: -91, y: 70, z: 29", v.toChatFormat())
        assertEquals("-91 70 29", v.toLocalFormat())
        assertEquals("-91.7 70.0 29.3", v.formatRounded(10))
    }
}
