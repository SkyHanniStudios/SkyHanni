package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator.at
import at.hannibal2.skyhanni.data.jsonobjects.repo.neu.NeuAttributeShardData
import at.hannibal2.skyhanni.data.jsonobjects.repo.neu.NeuAttributeShardJson
import at.hannibal2.skyhanni.features.inventory.attribute.AttributeShardsData
import at.hannibal2.skyhanni.features.inventory.attribute.AttributeShardsData.migrateLegacyStorage
import at.hannibal2.skyhanni.features.inventory.attribute.AttributeShardsData.shardStorageKey
import at.hannibal2.skyhanni.features.inventory.attribute.AttributeShardsData.syphonedAtTier
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import com.google.gson.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated

@Isolated
class AttributeShardProgressTest {

    private val costs = listOf(1, 3, 5)

    private val packrat = "ATTRIBUTE_SHARD_FIESTA_FORTUNE;1".toInternalName()
    private val chameleon = "ATTRIBUTE_SHARD_REPTILOID;1".toInternalName()

    private fun withMenuShards(check: AttributeShardsData.() -> Unit) {
        val packratData = NeuAttributeShardData(
            bazaarName = "SHARD_PACKRAT_SKULL".toInternalName(),
            displayName = "Packrat",
            rarity = LorenzRarity.UNCOMMON,
            internalName = packrat,
            abilityName = "Fiesta Fortune",
            family = emptyList(),
            alignment = "Forest",
            shardId = "U73",
        )
        val chameleonData = packratData.copy(
            bazaarName = "SHARD_CHAMELEON".toInternalName(),
            displayName = "Chameleon",
            internalName = chameleon,
            rarity = LorenzRarity.LEGENDARY,
            abilityName = "Reptiloid",
            shardId = "L4",
        )
        val repo = NeuAttributeShardJson(emptyMap(), listOf("SHARD_CHAMELEON"), listOf(packratData, chameleonData))
        try {
            AttributeShardsData.loadShardData(repo)
            AttributeShardsData.check()
        } finally {
            AttributeShardsData.loadShardData(NeuAttributeShardJson(emptyMap(), emptyList(), emptyList()))
        }
    }

    @Test
    fun `Packrat source ID takes precedence over a different known ability`() = withMenuShards {
        assertEquals(packrat, resolveMenuShardInternalName("Reptiloid I", listOf("Source: Packrat Shard (U73)")))
        assertEquals(packrat, resolveMenuShardInternalName("Reptiloid I", listOf("Source: Chameleon Shard (U73)")))
    }

    @Test
    fun `current inventory only counts shards with levels`() = withMenuShards {
        val items = setOf(packrat, chameleon, "ARROW".toInternalName())
        assertEquals(setOf(packrat), items.filterTo(mutableSetOf(), this::isTrackedShard))
    }

    @Test
    fun `Hunting Box accepts both tier formats for the matching ability`() {
        val ability = "Diurnal Animal"
        assertEquals(5, AttributeShardsData.readHuntingBoxTier(listOf("Diurnal Animal V (Foraging)"), ability))
        assertEquals(5, AttributeShardsData.readHuntingBoxTier(listOf("Diurnal Animal 5 (Foraging)"), ability))
        assertNull(AttributeShardsData.readHuntingBoxTier(listOf("Something else (Foraging)", "Owned: 1 Shard"), ability))
    }

    @Test
    fun `Packrat before its first syphon is distinct from an entry with no level data`() {
        val ability = "Fiesta Fortune"
        val owned = listOf("Owned: 1 Shard", "Syphon 1 more to level up!")
        assertNull(AttributeShardsData.readHuntingBoxTier(owned, ability))
        assertEquals(0, AttributeShardsData.readHuntingBoxTier(owned + "Fiesta Fortune (Mining)", ability))
    }

    @Test
    fun `an explicit maxed line is enough to identify the last tier`() {
        assertEquals(10, AttributeShardsData.readHuntingBoxTier(listOf("Attribute Maxed!"), "Fiesta Fortune"))
    }

    @Test
    fun `only explicit Yes and No lines change the shard state`() {
        assertEquals(true, AttributeShardsData.readAttributeEnabled(listOf("Fiesta Fortune", "Enabled: Yes")))
        assertEquals(false, AttributeShardsData.readAttributeEnabled(listOf("Enabled: No")))
        assertNull(AttributeShardsData.readAttributeEnabled(listOf("Fiesta Fortune")))
    }

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
        val rainbug = JsonObject().apply {
            addProperty("amountSyphoned", 5)
            addProperty("amountInBox", 2)
        }
        val profilePath = "storage.players.0a2af95e-21cf-4ee2-8a96-e30352680646.profiles.pear"
        val old = JsonObject()
        val profile = old.at(profilePath.split("."), true) as JsonObject
        profile.add("attributeShards", JsonObject().apply { add("", rainbug) })

        val event = ConfigUpdaterMigrator.ConfigFixEvent(
            old = old,
            new = JsonObject(),
            oldVersion = 149,
            movesPerformed = 0,
            dynamicPrefix = mapOf("#profile" to listOf(profilePath)),
        )
        AttributeShardsData.onConfigFix(event)

        val migrated = checkNotNull(event.new.at("$profilePath.attributeShards".split("."), false) as? JsonObject)
        val key = shardStorageKey("", "Rainbug")
        assertEquals("SHARD_RAINBUG", key)
        assertFalse(migrated.has(""))
        assertEquals(rainbug, migrated[key])
    }

    @Test
    fun `an existing Rainbug entry takes precedence over old data`() {
        val current = JsonObject().apply { addProperty("amountInBox", 3) }
        val key = shardStorageKey("", "Rainbug")
        val data = JsonObject().apply {
            add("", JsonObject())
            add(key, current)
        }
        migrateLegacyStorage(data)
        assertFalse(data.has(""))
        assertEquals(current, data[key])
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
