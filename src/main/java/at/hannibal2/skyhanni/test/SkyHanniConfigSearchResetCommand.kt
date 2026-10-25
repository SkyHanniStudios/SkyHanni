package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigFileType
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator.at
import at.hannibal2.skyhanni.config.SkyHanniConfig
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatIntOrNull
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.ReflectionUtils.getPrivateFieldValue
import at.hannibal2.skyhanni.utils.ReflectionUtils.makeAccessible
import at.hannibal2.skyhanni.utils.json.Shimmy
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.github.notenoughupdates.moulconfig.observer.Property
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.net.URI
import java.nio.file.Path
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAmount
import java.util.Date
import java.util.UUID

// TODO in the future change something here
//  ^ Convert the command functions to suspend funs, use coroutines
@SkyHanniModule
object SkyHanniConfigSearchResetCommand {

    private var lastCommand = emptyArray<String>()

    private fun runCommand(args: Array<String>): String = if (args.isEmpty()) {
        "§cThis is a powerful config-edit command, only use it if you know what you are doing!"
    } else when (args[0].lowercase()) {
        "reset" -> resetCommand(args)
        "search" -> searchCommand(args)
        "set" -> setCommand(args)
        "toggle" -> toggleCommand(args)
        "save" -> saveCommand()
        "autosave" -> autoSaveCommand()
        "raw" -> rawCommand(args)
        "version" -> versionCommand(args)
        else -> "§c/shconfig <search;reset;set;toggle;save;autosave;raw;version>"
    }

    fun resetCommand(args: Array<String>): String {
        if (args.size != 2) return "§c/shconfig reset <config element>"
        val term = args[1]
        if (term.startsWith("playerSpecific")) return "§cCannot reset playerSpecific! Use §e/shconfig set §cinstead."
        if (term.startsWith("profileSpecific")) return "§cCannot reset profileSpecific! Use §e/shconfig set §cinstead."

        try {
            val (field, defaultObject, _) = getComplexField(term, SkyHanniConfig())
            val (_, _, parent) = getComplexField(term, SkyHanniMod.feature)
            val affectedElements = findConfigElements({ it.startsWith("$term.") }, { true }).size
            if (affectedElements > 3 && !args.contentEquals(lastCommand)) {
                return "§cThis will change $affectedElements config elements! Use the command again to confirm."
            }
            field.set(parent, defaultObject)
            return "§eSuccessfully reset config element '$term'"
        } catch (e: Throwable) {
            ErrorManager.logErrorWithData(
                e, "Could not reset config element '$term'",
                "term" to term,
                "args" to args.joinToString(" "),
            )
            return "§cCould not reset config element '$term'"
        }
    }

    private fun searchCommand(args: Array<String>): String {
        if (args.size == 1) return "§c/shconfig search <config name> [class name]"

        return try {
            startSearch(args)
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e, "Error while trying to search config",
                "args" to args.joinToString(" "),
            )
            "§cError while trying to search config"
        }
    }

    private fun setCommand(args: Array<String>): String {
        if (args.size < 3) return "§c/shconfig set <config name> <json element>"
        val term = args[1]
        val rawJson = readValue(args) ?: return "§cClipboard has no string!"

        val shimmy = findShimmy(term) { return it }

        val affectedElements = findConfigElements({ it.startsWith("$term.") }, { true }).size
        if (affectedElements > 3 && !args.contentEquals(lastCommand)) {
            return "§cThis will change $affectedElements config elements! Use the command again to confirm."
        }

        val element = ConfigManager.gson.fromJson(rawJson, JsonElement::class.java)
        return try {
            shimmy.setJson(element)
            "§eChanged config element $term to ${shortValue(rawJson)}."
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e, "Could not change config element",
                "old" to shimmy.getJson(),
                "term" to term,
                "rawJson" to rawJson,
                "args" to args.joinToString(" "),
            )
            "§cCould not change config element '$term' to '${shortValue(rawJson)}'"
        }
    }

    private fun toggleCommand(args: Array<String>): String {
        if (args.size == 1 || args.size == 3) return "§c/shconfig toggle <config name> [value 1] [value 2]"

        val path = args[1]
        val rawJson1 = if (args.size > 2) args[2] else "true"
        val rawJson2 = if (args.size > 2) args[3] else "false"

        return try {
            // reads exactly this path, a contains search would also match longer paths that start the same
            val currentValue = findShimmy(path) { return it }.getJson()
            val firstValue = ConfigManager.gson.fromJson(rawJson1, JsonElement::class.java)
            val newValue = if (currentValue == firstValue) rawJson2 else rawJson1

            setCommand(arrayOf("set", path, newValue))
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e, "Error while trying to toggle config element",
                "path" to path,
                "rawJson1" to rawJson1,
                "rawJson2" to rawJson2,
            )
            "§cError while trying to toggle config element"
        }
    }

    /** Resolves a /shconfig path. Calls [onError] with the message for the player when the path does not resolve. */
    private inline fun findShimmy(term: String, onError: (String) -> Nothing): Shimmy {
        val root: Any = when {
            term.startsWith("config") -> SkyHanniMod.feature
            term.startsWith("playerSpecific") -> ProfileStorageData.playerSpecific ?: onError("§cplayerSpecific is null!")
            term.startsWith("profileSpecific") -> ProfileStorageData.profileSpecific ?: onError("§cprofileSpecific is null!")
            else -> onError("§cUnknown config location!")
        }
        return Shimmy(root, term.split(".").drop(1)) ?: onError("§cCould not change config element '$term', not found!")
    }

    private fun saveCommand(): String {
        val failed = SkyHanniMod.configManager.saveAll("manual-command")
        if (failed.isEmpty()) return "§eSaved all config files."
        return "§cCould not save: ${failed.joinToString { it.fileName }}"
    }

    private fun autoSaveCommand(): String {
        val configManager = SkyHanniMod.configManager
        configManager.autoSave = !configManager.autoSave
        if (configManager.autoSave) return "§eAuto save is on again."
        val writers = "§b/shconfig save§e, §braw §eand §bversion"
        return "§eAuto save is off until the next restart. Only $writers §ecan still write the config files."
    }

    /**
     * The value argument of set and raw: every argument after the config path joined with spaces, or the clipboard
     * text when that argument is "clipboard". Null when the clipboard has no text.
     */
    private fun readValue(args: Array<String>): String? {
        val value = args.drop(2).joinToString(" ")
        return if (value == "clipboard") OSUtils.readFromClipboard() else value
    }

    /** Shortens a value for a chat message, a value from the clipboard can be very long. */
    private fun shortValue(rawJson: String): String =
        if (rawJson.length <= 80) rawJson else "${rawJson.take(50)}... (${rawJson.length} characters)"

    private fun rawCommand(args: Array<String>): String {
        if (args.size < 2) return "§c/shconfig raw <file>.<path> [json element|clipboard]"
        val term = args[1]
        val fileName = term.substringBefore(".")
        val fileType = ConfigFileType.entries.find { it.fileName == fileName }
            ?: return "§cUnknown config file '$fileName'! Known: ${ConfigFileType.entries.joinToString { it.fileName }}"
        if (!fileType.file.exists()) return "§c$fileName.json does not exist yet."
        val path = term.split(".").drop(1)
        if (path.any { it.isEmpty() }) return "§cThe path '$term' has an empty part."
        val json = SkyHanniMod.configManager.readFileJson(fileType)
        return if (args.size == 2) readRaw(term, fileType, path, json) else writeRaw(args, term, fileType, path, json)
    }

    private fun readRaw(term: String, fileType: ConfigFileType, path: List<String>, json: JsonObject): String {
        val element = json.at(path, init = false) ?: return "§cNothing at '$term' in ${fileType.fileName}.json."
        OSUtils.copyToClipboard(ConfigManager.gson.toJson(element))
        return "§eCopied '$term' from ${fileType.fileName}.json to the clipboard."
    }

    private fun writeRaw(args: Array<String>, term: String, fileType: ConfigFileType, path: List<String>, json: JsonObject): String {
        if (SkyHanniMod.configManager.autoSave) {
            return "§cTurn auto save off first with §b/shconfig autosave§c, otherwise the next save overwrites ${fileType.fileName}.json."
        }
        if (path.isEmpty()) return "§c/shconfig raw needs a path inside the file to write to."
        val rawJson = readValue(args) ?: return "§cClipboard has no string!"
        val element = ConfigManager.gson.fromJson(rawJson, JsonElement::class.java)
        val parent = json.at(path.dropLast(1), init = true) as? JsonObject
            ?: return "§cCannot write into '$term', a part of the path is not an object."
        parent.add(path.last(), element)
        SkyHanniMod.configManager.writeFileJson(fileType, json)
        val target = "'$term' in ${fileType.fileName}.json"
        val hint = "Restart with auto save still off to load it. §b/shconfig save §ewould overwrite it."
        return "§eWrote ${shortValue(rawJson)} to $target. $hint"
    }

    private fun versionCommand(args: Array<String>): String {
        if (args.size > 1) {
            val version = args[1].formatIntOrNull() ?: return "§c/shconfig version [number]"
            return rawCommand(arrayOf("raw", "config.lastVersion", version.toString()))
        }
        if (!ConfigFileType.FEATURES.file.exists()) return "§cconfig.json does not exist yet."
        val fileVersion = SkyHanniMod.configManager.readFileJson(ConfigFileType.FEATURES)["lastVersion"]
        return "§econfig.json has lastVersion §b$fileVersion§e, the code is at §b${ConfigUpdaterMigrator.CONFIG_VERSION}§e."
    }

    private fun createFilter(condition: Boolean, searchTerm: () -> String): Pair<(String) -> Boolean, String> {
        return if (condition && searchTerm() != "all") {
            val term = searchTerm()
            Pair({ it.lowercase().contains(term) }, "'$term'")
        } else Pair({ true }, "<all>")
    }

    private fun startSearch(args: Array<String>): String {
        val (configFilter, configSearchTerm) = createFilter(true) { args[1].lowercase() }
        val (classFilter, classSearchTerm) = createFilter(args.size == 3) { args[2].lowercase() }

        val elements = findConfigElements(configFilter, classFilter)
        val builder = StringBuilder()
        builder.append("```\n")
        builder.append("Search config for SkyHanni ${SkyHanniMod.VERSION}\n")
        builder.append("configSearchTerm: $configSearchTerm\n")
        builder.append("classSearchTerm: $classSearchTerm\n")
        builder.append("\n")
        val size = elements.size
        builder.append("Found $size config elements:\n")
        for (entry in elements) {
            builder.append(entry)
            builder.append("\n")
        }
        builder.append("```")
        OSUtils.copyToClipboard(builder.toString())
        return "§eCopied search result ($size) to clipboard."
    }

    private fun findConfigElements(
        configFilter: (String) -> Boolean,
        classFilter: (String) -> Boolean,
        onlyValue: Boolean = false,
    ): MutableList<String> {
        val list = mutableListOf<String>()

        val map = buildMap {
            putAll(loadAllFields("config", SkyHanniMod.feature))

            val playerSpecific = ProfileStorageData.playerSpecific
            if (playerSpecific != null) {
                putAll(loadAllFields("playerSpecific", playerSpecific))
            } else {
                this["playerSpecific"] = null
            }

            val profileSpecific = ProfileStorageData.profileSpecific
            if (profileSpecific != null) {
                putAll(loadAllFields("profileSpecific", profileSpecific))
            } else {
                this["profileSpecific"] = null
            }
        }

        for ((name, obj) in map) {
            if (name == "config.DISCORD") continue
            if (name == "config.GITHUB") continue

            // this is the old, unused storage area
            if (name.startsWith("config.hidden")) continue

            if (name == "config.storage.players") continue
            if (name == "playerSpecific.profiles") continue

            val description = if (obj != null) {
                val className = obj.getClassName()
                if (!classFilter(className)) continue
                val objectName = obj.getObjectName()
                if (obj !is Runnable &&
                    objectName.startsWith(className) &&
                    (
                        objectName.startsWith("at.hannibal2.skyhanni.config.features.") ||
                            objectName.startsWith("at.hannibal2.skyhanni.config.storage.Storage")
                        )
                ) {
                    "<category>"
                } else if (onlyValue) {
                    objectName
                } else {
                    "$className = $objectName"
                }
            } else "null"

            if (configFilter(name)) {
                if (onlyValue) {
                    list.add(description)
                } else {
                    list.add("$name $description")
                }
            }
        }
        return list
    }

    private fun getComplexField(term: String, startObject: Any): Triple<Field, Any, Any> {
        var parentObject = startObject
        var obj = startObject
        val line = term.split(".").drop(1)
        var field: Field? = null
        for (entry in line) {
            field = obj.javaClass.getDeclaredField(entry).makeAccessible()
            parentObject = obj
            obj = field.get(obj)
        }
        if (field == null) {
            throw Error("Could not find field for '$term'")
        }
        return Triple(field, obj, parentObject)
    }

    @Suppress("CyclomaticComplexMethod")
    private fun loadAllFields(parentName: String, obj: Any, depth: Int = 0): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        if (depth == 8) { // this is only a backup for safety, needs increasing someday maybe
            map["$parentName.<end of depth>"] = null
            return map
        }
        if (obj.javaClass.`package`?.name?.startsWith("java.") == true) {
            return map
        }
        for (field in obj.javaClass.declaredFields) {
            if (Modifier.isStatic(field.modifiers)) continue

            val name = field.name
            if (parentName == "playerSpecific" && name == "profiles") continue
            if (parentName == "config.storage" && name == "players") continue
            if (parentName == "config" && name == "storage") continue
            val fieldName = "$parentName.$name"

            try {
                val value = field.makeAccessible().get(obj)
                map[fieldName] = value
                if (!isLeaf(value)) {
                    map.putAll(loadAllFields(fieldName, value, depth + 1))
                }
            } catch (_: Throwable) {
                SkyHanniMod.logger.warn("Could not access field '$fieldName' in class '${obj.javaClass.name}'")
                continue
            }
        }

        return map
    }

    private fun isLeaf(v: Any?): Boolean = v == null ||
        v is Boolean || // true, false
        v is Char || // 'x'
        v is Number || // Int, Long, Double, Float, Byte, Short, BigDecimal
        v is String || // "hello"
        v is Map<*, *> || // any Map<_, _>
        v is Collection<*> || // List, Set, Queue
        v is TemporalAccessor || // LocalDate, Instant, LocalTime
        v is TemporalAmount || // Duration, Period
        v is Date || // java.util.Date
        v is UUID || // java.util.UUID
        v is URI || // java.net.URI
        v is Path || // java.nio.file.Path
        v is File || // java.io.File
        v.javaClass.isArray || // Array<*>, IntArray, String[]
        v.javaClass.isEnum // any enum type


    private fun Any.getClassName(): String {
        if (this is Property<*>) {
            val value = getPrivateFieldValue("value")
            val name = value.getClassName()
            return "moulconfig.Property<$name>"
        }

        if (this is Runnable) return "Runnable"

        // we don't use javaClass.simpleName since we want to catch edge cases
        return when (val name = javaClass.name) {
            "at.hannibal2.skyhanni.config.core.config.Position" -> "Position"
            "java.lang.Boolean" -> "Boolean"
            "java.lang.Integer" -> "Int"
            "java.lang.Long" -> "Long"
            "java.lang.String" -> "String"
            "java.lang.Double" -> "Double"
            "io.github.notenoughupdates.moulconfig.observer.Property" -> "moulconfig.Property"
            "com.google.gson.internal.LinkedTreeMap" -> "LinkedTreeMap"
            "java.util.ArrayList" -> "List"
            "java.util.HashMap" -> "Map"

            else -> name
        }
    }

    private fun Any.getObjectName(): String = when (this) {
        is Position -> {
            val x = getPrivateFieldValue("x")
            val y = getPrivateFieldValue("y")
            val scale = getPrivateFieldValue("scale")
            "($x, $y, $scale)"
        }

        is String if isEmpty() -> "<empty string>"
        is String -> "'$this'"
        is Property<*> -> getPrivateFieldValue("value").getObjectName()
        is Int -> addSeparators()
        is Long -> addSeparators()
        is Runnable -> "<Runnable>"
        else -> toString()
    }

    @HandleEvent
    private fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.registerBrigadier("shconfig") {
            description = "Searches, changes and saves the config §c(warning, dangerous!)"
            category = CommandCategory.DEVELOPER_DEBUG
            legacyCallbackArgs {
                SkyHanniMod.launchCoroutine("shconfig command") {
                    ChatUtils.chat(runCommand(it))
                }
                lastCommand = it
            }
        }
    }

    @HandleEvent
    private fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Config Saving")
        if (SkyHanniMod.configManager.autoSave) {
            event.addIrrelevant("auto save is on")
        } else {
            event.addData("auto save is off, only /shconfig save, raw and version can write the config files")
        }
    }
}
