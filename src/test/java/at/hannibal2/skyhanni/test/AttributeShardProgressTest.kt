package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.features.inventory.attribute.syphonedAtTier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AttributeShardProgressTest {

    private val costs = listOf(1, 3, 5)

    @Test
    fun `missing progress uses the current tier`() {
        assertEquals(0, syphonedAtTier(costs, 0, null))
        assertEquals(4, syphonedAtTier(costs, 2, null))
    }

    @Test
    fun `remaining shards are subtracted from the next threshold`() {
        assertEquals(6, syphonedAtTier(costs, 2, 3))
        assertEquals(9, syphonedAtTier(costs, 2, 0))
    }

    @Test
    fun `max tier uses the full total`() {
        assertEquals(9, syphonedAtTier(costs, 3, null))
        assertEquals(9, syphonedAtTier(costs, 3, 0))
    }

    @Test
    fun `inconsistent progress is ignored`() {
        assertNull(syphonedAtTier(costs, 4, null))
        assertNull(syphonedAtTier(costs, 2, 6))
    }
}
