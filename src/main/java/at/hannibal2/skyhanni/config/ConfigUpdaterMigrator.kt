package at.hannibal2.skyhanni.config

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.utils.SkyHanniLogger
import at.hannibal2.skyhanni.utils.json.asIntOrNull
import at.hannibal2.skyhanni.utils.json.shDeepCopy
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.jetbrains.annotations.VisibleForTesting

object ConfigUpdaterMigrator {
    val logger = SkyHanniLogger("ConfigMigration")
    private const val CONFIG_VERSION = 149

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
        val config: JsonObject,
        val dynamicPrefix: Map<String, List<String>>,
        private val currentVersion: Int = CONFIG_VERSION,
    ) : SkyHanniEvent() {
        private data class Migration(
            val id: String,
            val since: Int?,
            val reads: Set<String>,
            val writes: Set<String>,
            val apply: (JsonObject) -> Unit,
        )

        private val migrations = mutableListOf<Migration>()
        val registeredMigrationIds: Set<String>
            get() = migrations.mapTo(mutableSetOf()) { it.id }

        fun transform(path: String, transform: (JsonElement) -> JsonElement = { it }) {
            transform(path, null, transform)
        }

        fun transform(
            path: String,
            discriminator: String?,
            transform: (JsonElement) -> JsonElement = { it },
        ) {
            register(transformId(path, discriminator), null, setOf(path), setOf(path), true) { config ->
                expand(path).forEach { path -> updateAt(config, path) { transform(it.shDeepCopy()) } }
            }
        }

        fun transform(since: Int, path: String, transform: (JsonElement) -> JsonElement = { it }) {
            transform(since, path, null, transform)
        }

        fun transform(
            since: Int,
            path: String,
            discriminator: String?,
            transform: (JsonElement) -> JsonElement = { it },
        ) {
            register(
                "transform@$since:${transformId(path, discriminator)}",
                since,
                setOf(path),
                setOf(path),
                currentVersion < since,
            ) { config ->
                expand(path).forEach { realPath -> updateAt(config, realPath) { transform(it.shDeepCopy()) } }
            }
        }

        fun add(path: String, value: () -> JsonElement) {
            addInternal(path, value, true, null, false)
        }

        fun add(since: Int, path: String, value: () -> JsonElement) {
            addInternal(path, value, currentVersion < since, since, true)
        }

        private fun addInternal(
            path: String,
            value: () -> JsonElement,
            shouldRun: Boolean,
            since: Int?,
            overwrite: Boolean,
        ) {
            register(
                if (since == null) "add:$path" else "add@$since:$path",
                since,
                emptySet(),
                setOf(path),
                shouldRun,
            ) { config ->
                expand(path).forEach { realPath ->
                    val parts = realPath.split(".")
                    (config.at(parts.dropLast(1), true) as? JsonObject)
                        ?.also { parent ->
                            if (overwrite || !parent.has(parts.last())) parent.add(parts.last(), value())
                        }
                }
            }
        }

        fun move(fromPath: String, toPath: String, transform: (JsonElement) -> JsonElement = { it }) {
            moveInternal(fromPath, toPath, transform, true, null)
        }

        fun move(
            since: Int,
            fromPath: String,
            toPath: String,
            transform: (JsonElement) -> JsonElement = { it },
        ) {
            moveInternal(fromPath, toPath, transform, currentVersion < since, since)
        }

        private fun moveInternal(
            fromPath: String,
            toPath: String,
            transform: (JsonElement) -> JsonElement,
            shouldRun: Boolean,
            since: Int?,
        ) {
            check(listOf(fromPath, toPath).none { it.startsWith("feature") }) {
                "Migration path should not start with 'features.'!"
            }
            require(!fromPath.substringBefore('.').startsWith("#") ||
                fromPath.substringBefore('.') == toPath.substringBefore('.')) {
                "Dynamic migration prefixes must match: $fromPath -> $toPath"
            }
            register(
                if (since == null) "move:$fromPath->$toPath" else "move@$since:$fromPath->$toPath",
                since,
                setOf(fromPath),
                setOf(toPath),
                shouldRun,
            ) { config ->
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
            removeInternal(path, true, null)
        }

        fun remove(since: Int, path: String) {
            removeInternal(path, currentVersion < since, since)
        }

        private fun removeInternal(path: String, shouldRun: Boolean, since: Int?) {
            register(
                if (since == null) "remove:$path" else "remove@$since:$path",
                since,
                setOf(path),
                emptySet(),
                shouldRun,
            ) { config ->
                expand(path).forEach { realPath ->
                    val parts = realPath.split(".")
                    (config.at(parts.dropLast(1), false) as? JsonObject)?.remove(parts.last())
                }
            }
        }

        private fun register(
            id: String,
            since: Int?,
            reads: Set<String>,
            writes: Set<String>,
            shouldRun: Boolean,
            apply: (JsonObject) -> Unit,
        ) {
            check(migrations.none { it.id == id }) { "Duplicate config migration ID: $id" }
            if (shouldRun) migrations += Migration(id, since, reads, writes, apply)
        }

        private fun transformId(path: String, discriminator: String?): String {
            return if (discriminator == null) "transform:$path" else "transform:$path#$discriminator"
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
            val ordered = remaining.filter { it.since != null }.sortedWith(
                compareBy<Migration> { it.since }.thenBy { it.id },
            ) + dependencySort(remaining.filter { it.since == null })
            ordered.forEach {
                try {
                    it.apply(config)
                    applied += it.id
                } catch (error: Throwable) {
                    logger.logError("Config migration ${it.id} failed; it will be retried", error)
                }
            }
        }

        private fun dependencySort(migrations: List<Migration>): List<Migration> {
            val dependencies = migrations.associateWith { candidate ->
                migrations.filter { other ->
                    other !== candidate && other.writes.any { write ->
                        candidate.reads.any { read -> pathsOverlap(read, write) }
                    }
                }.toMutableSet()
            }.toMutableMap()
            val ordered = mutableListOf<Migration>()
            while (dependencies.isNotEmpty()) {
                val ready = dependencies.filterValues { it.isEmpty() }.keys.minByOrNull { it.id }
                    ?: dependencies.keys.minByOrNull { it.id }
                    ?: error("No pending migrations")
                ordered += ready
                dependencies.remove(ready)
                dependencies.values.forEach { it.remove(ready) }
            }
            return ordered
        }

        private fun pathsOverlap(first: String, second: String): Boolean {
            return first == second || first.startsWith("$second.") || second.startsWith("$first.")
        }
    }

    fun fixConfig(config: JsonObject): JsonObject {
        val storedVersion = (config["lastVersion"] as? JsonPrimitive)?.asIntOrNull
        val hasLedger = config["appliedMigrations"] is JsonArray
        val applied = (config["appliedMigrations"] as? JsonArray)
            ?.mapNotNull { it.takeUnless(JsonElement::isJsonNull)?.asString }
            ?.toMutableSet() ?: mutableSetOf()
        val isFresh = storedVersion == null && applied.isEmpty()
        val oldVersion = storedVersion ?: if (hasLedger) CONFIG_VERSION else -1
        val event = ConfigFixEvent(
            config,
            dynamicPrefixes(config["storage"]?.asJsonObject),
            oldVersion,
        ).also { it.post() }
        if (isFresh) {
            applied += event.registeredMigrationIds
        } else {
            event.applyPending(applied)
        }
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
