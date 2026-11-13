package at.hannibal2.skyhanni.test.config

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator.at
import com.google.gson.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ConfigUpdaterMigratorTest {

    @Test
    fun `migrations are ordered and idempotent`() {
        val config = JsonObject().apply {
            addProperty("old", "value")
        }
        val applied = mutableSetOf<String>()

        fun run() {
            ConfigUpdaterMigrator.ConfigFixEvent(
                config, emptyMap(),
            ).apply {
                move("old", "middle")
                move("middle", "new")
                applyPending(applied)
            }
        }

        run()
        assertEquals("value", config["new"].asString)
        assertFalse(config.has("old"))
        assertFalse(config.has("middle"))
        val firstResult = config.toString()

        run()
        assertEquals(firstResult, config.toString())
        assertEquals(
            setOf("move:old->middle", "move:middle->new"),
            applied,
        )
    }

    @Test
    fun `versioned overload remains compatible`() {
        val old = JsonObject().apply { addProperty("old", "value") }
        val new = JsonObject()
        ConfigUpdaterMigrator.ConfigFixEvent(
            old, emptyMap(), currentVersion = 0,
        ).apply {
            move(149, "old", "new")
            applyPending(mutableSetOf())
        }

        assertEquals("value", old["new"].asString)
        assertFalse(old.has("old"))
    }

    @Test
    fun `versioned migration already covered by config version is not registered`() {
        val config = JsonObject().apply { addProperty("old", "value") }
        val applied = mutableSetOf<String>()

        ConfigUpdaterMigrator.ConfigFixEvent(
            config, emptyMap(), currentVersion = 150,
        ).apply {
            move(149, "old", "new")
            applyPending(applied)
        }

        assertEquals("value", config["old"].asString)
        assertFalse(config.has("new"))
        assertEquals(emptySet<String>(), applied)
    }

    @Test
    fun `missing source is still recorded as applied`() {
        val config = JsonObject()
        val applied = mutableSetOf<String>()

        ConfigUpdaterMigrator.ConfigFixEvent(config, emptyMap()).apply {
            move("missing", "new")
            applyPending(applied)
        }

        assertFalse(config.has("new"))
        assertEquals(setOf("move:missing->new"), applied)
    }

    @Test
    fun `dynamic prefixes migrate every resolved path`() {
        val config = JsonObject().apply {
            add("players", JsonObject().apply {
                add("alice", JsonObject().apply { addProperty("old", 1) })
                add("bob", JsonObject().apply { addProperty("old", 2) })
            })
        }
        val applied = mutableSetOf<String>()

        ConfigUpdaterMigrator.ConfigFixEvent(
            config,
            mapOf("#player" to listOf("players.alice", "players.bob")),
        ).apply {
            move("#player.old", "#player.new")
            applyPending(applied)
        }

        assertEquals(1, config.at(listOf("players", "alice", "new"), false)!!.asInt)
        assertEquals(2, config.at(listOf("players", "bob", "new"), false)!!.asInt)
    }

    @Test
    fun `duplicate migration ids fail fast`() {
        assertThrows(IllegalStateException::class.java) {
            ConfigUpdaterMigrator.ConfigFixEvent(
                JsonObject(), emptyMap(),
            ).apply {
                move("old", "new")
                move("old", "new")
            }
        }
    }
}
