package at.hannibal2.skyhanni.test.vector

import at.hannibal2.skyhanni.utils.json.BaseGsonBuilder
import at.hannibal2.skyhanni.utils.vector.SkyHanniBlockVector
import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VectorSerializationTest {

    private val gson = BaseGsonBuilder.gson().create()

    private inline fun <reified T> fromJson(json: String): T = gson.fromJson(json, object : TypeToken<T>() {}.type)

    @Test
    fun `vectors keep the LorenzVec wire format`() {
        assertEquals("\"1.5:-2.0:3.0\"", gson.toJson(SkyHanniVector(1.5, -2.0, 3.0)))
        assertEquals("\"1:-2:3\"", gson.toJson(SkyHanniBlockVector(1, -2, 3)))
    }

    @Test
    fun `block vectors load LorenzVec doubles`() {
        assertEquals(SkyHanniBlockVector(1, 2, 3), fromJson<SkyHanniBlockVector>("\"1.0:2.0:3.0\""))
    }

    @Test
    fun `map keys round trip`() {
        val byPosition = mapOf(SkyHanniVector(-1.5, 0.0, 1.0) to 7)
        val byBlock = mapOf(SkyHanniBlockVector(-1, 0, 1) to 9)
        assertEquals(byPosition, fromJson<Map<SkyHanniVector, Int>>(gson.toJson(byPosition)))
        assertEquals(byBlock, fromJson<Map<SkyHanniBlockVector, Int>>(gson.toJson(byBlock)))
        assertEquals(byBlock, fromJson<Map<SkyHanniBlockVector, Int>>("{\"-1.0:0.0:1.0\": 9}"))
    }
}
