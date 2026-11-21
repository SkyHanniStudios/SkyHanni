package at.hannibal2.skyhanni.features.misc.update

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.SkyHanniMod.launch
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.config.commands.brigadier.BrigadierArguments
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.toQueryString
import at.hannibal2.skyhanni.utils.api.ApiUtils
import at.hannibal2.skyhanni.utils.collection.CollectionUtils.containsKeys
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import at.hannibal2.skyhanni.utils.coroutines.CoroutineSettings
import at.hannibal2.skyhanni.utils.json.fromJson
import at.hannibal2.skyhanni.utils.system.ModVersion
import com.google.gson.Gson
import moe.nea.libautoupdate.GithubReleaseUpdateSource.GithubRelease
import java.util.NavigableMap
import java.util.TreeMap
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.future.await

@SkyHanniModule
object ChangelogViewer {

    private val gson = Gson()

    internal val cache: NavigableMap<ModVersion, Map<String, List<String>>> = TreeMap()

    internal var openTime = SimpleTimeMark.farPast()

    internal lateinit var startVersion: ModVersion
    internal lateinit var endVersion: ModVersion

    internal var shouldMakeNewList = false
    private val dataFetchCoroutine = CoroutineSettings(
        "changelog viewer fetch data", timeout = 15.seconds,
    ).withIOContext()
    private var fetchJob: Job? = null

    internal var shouldShowBeta = SkyHanniMod.isBetaVersion
    internal var showTechnicalDetails = false

    fun showChangelog(currentVersion: String, targetVersion: String) =
        showChangelog(ModVersion.fromString(currentVersion), ModVersion.fromString(targetVersion))

    private fun showChangelog(currentVersion: ModVersion, targetVersion: ModVersion) {
        if (currentVersion > targetVersion) {
            ErrorManager.logErrorStateWithData(
                "Invalid versions for changelog",
                "current version is larger than target version",
                "current" to currentVersion,
                "target" to targetVersion,
            )
            return
        }
        startVersion = currentVersion
        endVersion = targetVersion
        if (!cache.containsKeys(startVersion, endVersion)) setupFetchJob()
        openChangelog()
    }

    private fun setupFetchJob() {
        if (fetchJob?.isActive == true) return
        fetchJob = dataFetchCoroutine.launch { getChangelog() }
    }

    private fun openChangelog() {
        if (MinecraftCompat.screen !is ChangeLogViewerScreen) SkyHanniMod.screenToOpen = ChangeLogViewerScreen()
    }

    private suspend fun getChangelog() {
        when (val updateSource = SkyHanniMod.feature.dev.debug.updateSource.get()) {
            SkyHanniUpdateSource.MODRINTH -> {
                val source = updateSource.source as ModrinthUpdateSource
                source.getReleases(includeChangelog = true).await()
                    ?.forEach { release ->
                        cache[release.versionNumber] = formatChangelog(release.changelog.orEmpty())
                    }
                    ?: error("Changelog Loading Failed")
            }

            SkyHanniUpdateSource.GITHUB -> {
                val source = updateSource.source as CustomGithubReleaseUpdateSource
                buildList {
                    var pageNumber = 1
                    while (true) {
                        val pagedUrl = source.releaseApiUrl + mapOf(
                            "per_page" to 100,
                            "page" to pageNumber,
                        ).toQueryString()
                        val (_, jsonObject) = ApiUtils.getJsonResponse(pagedUrl, apiName = "github")
                            .assertSuccessWithData()
                            ?: error("Changelog Loading Failed")

                        // We cannot use ConfigManager.gson here because it excludes fields without
                        // @Expose annotations, and GithubRelease comes from libautoupdate
                        val page = gson.fromJson<List<GithubRelease>>(jsonObject)

                        addAll(page)

                        if (page.isEmpty()) break
                        if (ModVersion.fromString(page.last().tagName) <= startVersion) break

                        pageNumber++
                    }
                }
                    .forEach {
                        cache[ModVersion.fromString(it.tagName)] = formatChangelog(it.body.orEmpty())
                    }
            }
        }
    }

    private fun formatChangelog(body: String): Map<String, List<String>> =
        formatData(formatString(getBasic(body)))

    // These patterns parse internal changelog formatting, not Hypixel game messages, and do not need remote update capability.
    private val trailingNewlinePattern = "\\s*\r?\n$".toRegex()
    private val lineBreakPattern = "\r?\n".toRegex()

    private fun formatData(text: String): Map<String, List<String>> {
        var headline = 0
        return text // Bolding Markdown
            .replace(trailingNewlinePattern, "") // Remove trailing empty lines
            .split(lineBreakPattern) // Split at newlines
            .map { it.trimEnd() } // Remove trailing empty stuff
            .groupBy {
                if (it.startsWith("§l§9")) {
                    headline++
                }
                headline
            }
            // Change §a to §c if in removed
            .mapKeys { it.value.firstOrNull().orEmpty() }.toMutableMap().also { map ->
                val key = "§l§9Removed Features"
                val subgroup = map[key] ?: return@also
                map[key] = subgroup.map {
                    it.replace("§a", "§c")
                }
            }
    }

    private fun formatString(basic: String): String = basic.replace("\\*\\*(?<content>.*?)\\*\\*".toRegex()) {
        fun String.help(s: String): String =
            toRegex().find(basic.subSequence(0, it.range.first).reversed())?.groups?.get(s)?.value?.reversed().orEmpty()

        val format = "\n|(?<format>[kmolnrKMOLNR]§)".help("format")
        val color = "\n|(?<color>[0-9a-fA-F]§)".help("color")
        val content = it.groups["content"]?.value.orEmpty()
        "§l$content§r$format$color"
    }

    private fun getBasic(body: String): String = body.replace("[^]]\\(https://github[\\w/.?$&#]*\\)".toRegex(), "") // Remove GitHub link
        .replace("#+\\s*".toRegex(), "§l§9") // Formatting for headings
        .replace("(\n[ \t]+)[+\\-*][^+\\-*]".toRegex(), "$1§7") // Formatting for subpoints
        .replace("\n[+\\-*][^+\\-*]".toRegex(), "\n§a") // Formatting for points
        .replace("(- [^-\r\n]*(?:\r\n|$))".toRegex(), "§b§l$1") // Color contributors
        .replace("\\[(.+?)]\\(.+?\\)".toRegex(), "$1") // Random links
        .replace("`", "\"") // Fix code blocks to look better
        .replace("§l§9(?:Version|SkyHanni)[^\r\n]*\r\n".toRegex(), "") // Remove version from body

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.registerBrigadier("shchangelog") {
            description = "Shows the specified changelog. No arguments shows the latest changelog."
            category = CommandCategory.USERS_ACTIVE

            simpleCallback { runChangelog(null, null) }

            literal("show") {
                argCallback("version", BrigadierArguments.string()) { versionStr ->
                    val v = parseVersion(versionStr) ?: return@argCallback
                    showChangelog(v, v)
                }
            }

            literal("since") {
                argCallback("version", BrigadierArguments.string()) { versionStr ->
                    val since = parseVersion(versionStr) ?: return@argCallback
                    runChangelog(since, null)
                }
            }

            literal("until") {
                argCallback("version", BrigadierArguments.string()) { versionStr ->
                    val until = parseVersion(versionStr) ?: return@argCallback
                    runChangelog(null, until)
                }
            }

            arg("sinceVersion", BrigadierArguments.string()) { sinceArg ->
                argCallback("untilVersion", BrigadierArguments.string()) { untilStr ->
                    val since = parseVersion(getArg(sinceArg)) ?: return@argCallback
                    val until = parseVersion(untilStr) ?: return@argCallback
                    if (until < since) {
                        ChatUtils.userError("until '$until' must be greater than since '$since'")
                        return@argCallback
                    }
                    showChangelog(since, until)
                }
                callback {
                    val since = parseVersion(getArg(sinceArg)) ?: return@callback
                    runChangelog(since, null)
                }
            }
        }
    }

    private fun runChangelog(since: ModVersion?, until: ModVersion?) {
        val resolvedSince = since ?: ModVersion.fromString(SkyHanniMod.VERSION)
        val resolvedUntil = until ?: ModVersion.fromString(UpdateManager.getNextVersion() ?: SkyHanniMod.VERSION)
        if (resolvedUntil < resolvedSince) {
            ChatUtils.userError("until '$resolvedUntil' is less than since '$resolvedSince'")
            return
        }
        showChangelog(resolvedSince, resolvedUntil)
    }

    private fun parseVersion(input: String): ModVersion? {
        val version = ModVersion.fromString(input)
        return if (!version.isValid()) {
            ChatUtils.userError(
                "'$input' is not a valid mod version. Version syntax: 'Major.Minor.Patch', " +
                    "anything not written is assumed 0. E.g. 1.1 = 1.1.0",
            )
            null
        } else version
    }
}
