package at.hannibal2.skyhanni.config

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.utils.SkyHanniLogger
import at.hannibal2.skyhanni.utils.json.shDeepCopy
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.jetbrains.annotations.VisibleForTesting

object ConfigUpdaterMigrator {
    val logger = SkyHanniLogger("ConfigMigration")
    const val CONFIG_VERSION = 149

    fun JsonElement.at(chain: List<String>, init: Boolean): JsonElement? {
        if (chain.isEmpty()) return this
        if (this !is JsonObject) return null
        var child = this[chain.first()]
        if (child == null && init) {
            child = JsonObject()
            add(chain.first(), child)
        }
        return child?.at(chain.drop(1), init)
    }

    @PrimaryFunction("onConfigFix")
    data class ConfigFixEvent(
        val old: JsonObject,
        val new: JsonObject,
        val dynamicPrefix: Map<String, List<String>>,
        private val currentVersion: Int = CONFIG_VERSION,
    ) : SkyHanniEvent() {
        constructor(
            old: JsonObject,
            new: JsonObject,
            oldVersion: Int,
            movesPerformed: Int,
            dynamicPrefix: Map<String, List<String>>,
        ) : this(old, new, dynamicPrefix, oldVersion)

        private data class Migration(
            val id: String,
            val reads: Set<String>,
            val writes: Set<String>,
            val apply: (JsonObject) -> Unit,
        )

        private val migrations = mutableListOf<Migration>()
        val registeredMigrationIds: Set<String>
            get() = migrations.mapTo(mutableSetOf()) { it.id }

        fun transform(path: String, transform: (JsonElement) -> JsonElement = { it }) {
            register("transform:$path", setOf(path), setOf(path), true) { config ->
                expand(path).forEach { path -> updateAt(config, path) { transform(it.shDeepCopy()) } }
            }
        }

        fun transform(since: Int, path: String, transform: (JsonElement) -> JsonElement = { it }) {
            transform(path, transform, since)
        }

        private fun transform(path: String, transform: (JsonElement) -> JsonElement, since: Int) {
            register("transform:$path", setOf(path), setOf(path), currentVersion < since) { config ->
                expand(path).forEach { path -> updateAt(config, path) { transform(it.shDeepCopy()) } }
            }
        }

        fun add(path: String, value: () -> JsonElement) {
            addInternal(path, value, true)
        }

        fun add(since: Int, path: String, value: () -> JsonElement) {
            addInternal(path, value, currentVersion < since)
        }

        private fun addInternal(path: String, value: () -> JsonElement, shouldRun: Boolean) {
            register("add:$path", emptySet(), setOf(path), shouldRun) { config ->
                expand(path).forEach { realPath ->
                    val parts = realPath.split(".")
                    (config.at(parts.dropLast(1), true) as? JsonObject)
                        ?.takeUnless { it.has(parts.last()) }
                        ?.add(parts.last(), value())
                }
            }
        }

        fun move(fromPath: String, toPath: String, transform: (JsonElement) -> JsonElement = { it }) {
            moveInternal(fromPath, toPath, transform, true)
        }

        fun move(
            since: Int,
            fromPath: String,
            toPath: String,
            transform: (JsonElement) -> JsonElement = { it },
        ) {
            moveInternal(fromPath, toPath, transform, currentVersion < since)
        }

        private fun moveInternal(
            fromPath: String,
            toPath: String,
            transform: (JsonElement) -> JsonElement,
            shouldRun: Boolean,
        ) {
            check(listOf(fromPath, toPath).none { it.startsWith("feature") }) {
                "Migration path should not start with 'features.'!"
            }
            require(!fromPath.substringBefore('.').startsWith("#") ||
                fromPath.substringBefore('.') == toPath.substringBefore('.')) {
                "Dynamic migration prefixes must match: $fromPath -> $toPath"
            }
            register("move:$fromPath->$toPath", setOf(fromPath), setOf(toPath), shouldRun) { config ->
                expand(fromPath).forEach { realFromPath ->
                    val realToPath = if (fromPath.startsWith("#")) {
                        realToPath(realFromPath, fromPath, toPath)
                    } else toPath
                    val source = config.at(realFromPath.split("."), false) ?: return@forEach
                    val targetParts = realToPath.split(".")
                    val target = config.at(targetParts.dropLast(1), true) as? JsonObject ?: return@forEach
                    target.add(targetParts.last(), transform(source.shDeepCopy()))
                    (config.at(realFromPath.split(".").dropLast(1), false) as? JsonObject)
                        ?.remove(realFromPath.substringAfterLast('.'))
                }
            }
        }

        fun remove(path: String) {
            removeInternal(path, true)
        }

        fun remove(since: Int, path: String) {
            removeInternal(path, currentVersion < since)
        }

        private fun removeInternal(path: String, shouldRun: Boolean) {
            register("remove:$path", setOf(path), emptySet(), shouldRun) { config ->
                expand(path).forEach { realPath ->
                    val parts = realPath.split(".")
                    (config.at(parts.dropLast(1), false) as? JsonObject)?.remove(parts.last())
                }
            }
        }

        private fun register(
            id: String,
            reads: Set<String>,
            writes: Set<String>,
            shouldRun: Boolean,
            apply: (JsonObject) -> Unit,
        ) {
            check(migrations.none { it.id == id }) { "Duplicate config migration ID: $id" }
            migrations += Migration(id, reads, writes, if (shouldRun) apply else { _ -> })
        }

        private fun expand(path: String): List<String> {
            val prefix = path.substringBefore('.')
            val suffix = path.substringAfter('.', "")
            if (!prefix.startsWith("#")) return listOf(path)
            return dynamicPrefix[prefix].orEmpty().map { if (suffix.isEmpty()) it else "$it.$suffix" }
        }

        private fun realToPath(realPath: String, fromPath: String, toPath: String): String {
            val fromSuffix = fromPath.substringAfter('.', "")
            val toSuffix = toPath.substringAfter('.', "")
            val base = realPath.removeSuffix(fromSuffix)
            return if (toSuffix.isEmpty()) base.trimEnd('.') else "$base$toSuffix"
        }

        private fun updateAt(config: JsonObject, path: String, transform: (JsonElement) -> JsonElement) {
            val parts = path.split(".")
            val parent = config.at(parts.dropLast(1), false) as? JsonObject ?: return
            parent[parts.last()]?.let { parent.add(parts.last(), transform(it)) }
        }

        @VisibleForTesting
        internal fun applyPending(applied: MutableSet<String>) {
            val remaining = migrations.filter { it.id !in applied }.toMutableSet()
            val ordered = mutableListOf<Migration>()
            while (remaining.isNotEmpty()) {
                val next = remaining.filter { candidate ->
                    remaining.none { other ->
                        other !== candidate && other.writes.any { write ->
                            candidate.reads.any { read -> read == write }
                        }
                    }
                }.minByOrNull { it.id } ?: error("Cyclic config migration dependencies: ${remaining.map { it.id }}")
                remaining.remove(next)
                ordered += next
            }
            ordered.forEach {
                it.apply(old)
                applied += it.id
            }
        }
    }

    fun fixConfig(config: JsonObject): JsonObject {
        val oldVersion = (config["lastVersion"] as? JsonPrimitive)?.asInt ?: -1
        val applied = (config["appliedMigrations"] as? JsonArray)
            ?.mapNotNull { it.takeUnless(JsonElement::isJsonNull)?.asString }
            ?.toMutableSet() ?: mutableSetOf()
        val event = ConfigFixEvent(
            config,
            config,
            dynamicPrefixes(config["storage"]?.asJsonObject),
            oldVersion,
        ).also { it.post() }
        event.applyPending(applied)
        config.remove("lastVersion")
        config.add("appliedMigrations", JsonArray().also { array -> applied.forEach(array::add) })
        return config
    }

    private fun dynamicPrefixes(storage: JsonObject?): Map<String, List<String>> = mapOf(
        "#profile" to (storage?.get("players")?.asJsonObject?.entrySet()?.flatMap { player ->
            player.value.asJsonObject["profiles"]?.asJsonObject?.entrySet()?.map {
                "storage.players.${player.key}.profiles.${it.key}"
            }.orEmpty()
        }.orEmpty()),
        "#player" to (storage?.get("players")?.asJsonObject?.entrySet()?.map {
            "storage.players.${it.key}"
        }.orEmpty()),
    )
}
