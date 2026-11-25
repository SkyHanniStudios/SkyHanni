package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.AttributeShardData
import at.hannibal2.skyhanni.features.inventory.attribute.AttributeShardsData.migrateLegacyStorage
import at.hannibal2.skyhanni.features.inventory.attribute.AttributeShardsData.shardStorageKey
import at.hannibal2.skyhanni.features.inventory.attribute.AttributeShardsData.syphonedAtTier
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

    @Test
    fun `Rainbug progress survives migration from an empty Bazaar key`() {
        val rainbug = AttributeShardData(amountSyphoned = 5, amountInBox = 2)
        val data = mutableMapOf("" to rainbug)
        val key = shardStorageKey("", "Rainbug")
        assertEquals("SHARD_RAINBUG", key)
        migrateLegacyStorage(data, key)
        migrateLegacyStorage(data, key)
        assertEquals(mapOf(key to rainbug), data)
    }

    @Test
    fun `an existing Rainbug entry takes precedence over old data`() {
        val current = AttributeShardData(amountInBox = 3)
        val key = shardStorageKey("", "Rainbug")
        val data = mutableMapOf("" to AttributeShardData(), key to current)
        migrateLegacyStorage(data, key)
        assertEquals(mapOf(key to current), data)
    }

    @Test
    fun `a missing repo entry leaves legacy progress intact`() {
        val legacy = AttributeShardData(amountInBox = 1)
        val data = mutableMapOf("" to legacy)
        migrateLegacyStorage(data, null)
        assertEquals(mapOf("" to legacy), data)
    }

    @Test
    fun `Packrat keeps its Bazaar key and unlocks after the first syphon`() {
        val uncommon = listOf(1, 2, 3, 4, 5, 6, 7, 8, 12, 16)
        assertEquals("SHARD_PACKRAT_SKULL", shardStorageKey("SHARD_PACKRAT_SKULL", "Packrat"))
        assertEquals(0, syphonedAtTier(uncommon, 0, 1))
        assertEquals(1, syphonedAtTier(uncommon, 1, 2))
    }

    @Test
    fun `Groundhog V with four shards remaining keeps its partial progress`() {
        val common = listOf(1, 3, 5, 6, 7, 8, 10, 14, 18, 24)
        assertEquals(26, syphonedAtTier(common, 5, 4))
        assertEquals(22, syphonedAtTier(common, 5, null))
    }
}
