package at.hannibal2.skyhanni.test.vector

import at.hannibal2.skyhanni.utils.json.BaseGsonBuilder
import at.hannibal2.skyhanni.utils.vector.KVector
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VectorSerializationTest {

    private val gson = BaseGsonBuilder.gson().create()

    private inline fun <reified T> fromJson(json: String): T = gson.fromJson(json, object : TypeToken<T>() {}.type)

    @Test
    fun `vectors keep the LorenzVec wire format`() {
        assertEquals("\"1.5:-2.0:3.0\"", gson.toJson(KVector(1.5, -2.0, 3.0)))
    }

    @Test
    fun `map keys round trip`() {
        val byPosition = mapOf(KVector(-1.5, 0.0, 1.0) to 7)
        assertEquals(byPosition, fromJson<Map<KVector, Int>>(gson.toJson(byPosition)))
    }

    @Test
    fun `negative zero keys load as positive zero`() {
        val loaded = fromJson<Map<KVector, Int>>("{\"-0.0:0.0:-0.0\": 7}")
        assertEquals(7, loaded[KVector.ZERO])
        assertEquals("\"0.0:0.0:0.0\"", gson.toJson(KVector(-0.0, 0.0, -0.0)))
    }
}
