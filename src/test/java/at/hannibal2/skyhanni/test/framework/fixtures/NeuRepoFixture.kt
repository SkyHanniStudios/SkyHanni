package at.hannibal2.skyhanni.test.framework.fixtures

import at.hannibal2.skyhanni.api.enoughupdates.EnoughUpdatesManager
import at.hannibal2.skyhanni.test.framework.MinecraftBootstrap
import at.hannibal2.skyhanni.test.framework.NeedsNeuRepo
import at.hannibal2.skyhanni.test.framework.TestFixture
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NeuItems
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.File

class NeuRepoFixture : TestFixture {

    fun load(vararg internalNames: String) {
        val wanted = internalNames.mapTo(mutableSetOf()) { it.toInternalName() }
        EnoughUpdatesManager.loadItemsForTest(if (wanted.isEmpty()) allItems else allItems.filterKeys { it in wanted })
        NeuItems.rebuildForTest()
    }

    override fun setUp(context: ExtensionContext) {
        MinecraftBootstrap.ensure()
        val items = context.requiredTestClass.getAnnotation(NeedsNeuRepo::class.java)?.items.orEmpty()
        load(*items)
    }

    override fun tearDown(context: ExtensionContext) {
        EnoughUpdatesManager.clearMaps()
        NeuItems.rebuildForTest()
    }

    private companion object {
        const val ARCHIVE_PROPERTY = "skyhanni.test.neuRepo"
        const val ITEMS_DIR = "items/"
        const val JSON_SUFFIX = ".json"

        val allItems: Map<NeuInternalName, JsonObject> by lazy { readArchive() }

        fun readArchive(): Map<NeuInternalName, JsonObject> {
            val path = System.getProperty(ARCHIVE_PROPERTY) ?: error("System property $ARCHIVE_PROPERTY is not set")
            val archive = File(path).takeIf { it.isFile } ?: error("NEU repo archive missing at $path, run includeBackupNeuRepo")
            val items = mutableMapOf<NeuInternalName, JsonObject>()
            TarArchiveInputStream(GzipCompressorInputStream(archive.inputStream().buffered())).use { tar ->
                generateSequence { tar.nextEntry }.forEach { entry ->
                    val relative = entry.name.substringAfter('/')
                    if (entry.isDirectory || !relative.startsWith(ITEMS_DIR) || !relative.endsWith(JSON_SUFFIX)) return@forEach
                    val internalName = relative.removePrefix(ITEMS_DIR).removeSuffix(JSON_SUFFIX)
                    if ('/' in internalName) return@forEach
                    items[internalName.toInternalName()] = JsonParser.parseString(tar.readBytes().decodeToString()).asJsonObject
                }
            }
            return items
        }
    }
}
