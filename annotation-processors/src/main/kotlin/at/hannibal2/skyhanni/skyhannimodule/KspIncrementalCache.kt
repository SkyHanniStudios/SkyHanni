package at.hannibal2.skyhanni.skyhannimodule

import java.io.File
import java.util.zip.CRC32

/**
 * Tracks source file states (mtime + CRC) across KSP runs to avoid unnecessary regeneration.
 *
 * @param cacheDir The directory to store the state file in, or null to disable caching.
 * @param mcVersion The Minecraft version string, used to namespace the state file.
 * @param stateFileName The base name for the state file (versioned automatically).
 */
class KspIncrementalCache(cacheDir: String?, mcVersion: String, stateFileName: String) {

    private val stateFile: File? = cacheDir?.let { File(it, "$stateFileName-$mcVersion.txt") }
    private var pendingStates: Map<String, FileState> = emptyMap()

    private data class FileState(val mtime: Long, val crc: Long)

    /**
     * @param filePaths The source file paths to evaluate against the cache.
     */
    fun evaluate(filePaths: Set<String>): Set<String> {
        val cachedStates = readStateFile()
        val newStates = mutableMapOf<String, FileState>()
        val dirtyPaths = mutableSetOf<String>()

        for (path in filePaths) {
            val mtime = File(path).lastModified()
            val cached = cachedStates?.get(path)
            if (cached != null && cached.mtime == mtime) {
                newStates[path] = cached
            } else {
                val crc = fileCrc(path)
                newStates[path] = FileState(mtime, crc)
                if (cached == null || cached.crc != crc) dirtyPaths.add(path)
            }
        }

        pendingStates = newStates
        return dirtyPaths
    }

    /** Persists the state computed during the last [evaluate] call. */
    fun commit(failedPaths: Set<String> = emptySet()) = writeStateFile(pendingStates - failedPaths)

    private fun fileCrc(path: String): Long {
        val crc = CRC32()
        crc.update(File(path).readBytes())
        return crc.value
    }

    private fun readStateFile(): Map<String, FileState>? {
        val file = stateFile?.takeIf { it.exists() } ?: return null
        return file.readLines().mapNotNull { line ->
            val hashIdx = line.lastIndexOf('|')
            if (hashIdx < 0) return@mapNotNull null
            val mtimeIdx = line.lastIndexOf('|', hashIdx - 1)
            if (mtimeIdx < 0) return@mapNotNull null
            val path = line.substring(0, mtimeIdx)
            val mtime = line.substring(mtimeIdx + 1, hashIdx).toLongOrNull() ?: return@mapNotNull null
            val crc = line.substring(hashIdx + 1).toLongOrNull() ?: return@mapNotNull null
            path to FileState(mtime, crc)
        }.toMap()
    }

    private fun writeStateFile(states: Map<String, FileState>) {
        val file = stateFile ?: return
        file.parentFile?.mkdirs()
        file.writeText(states.entries.joinToString("\n") { (path, state) -> "$path|${state.mtime}|${state.crc}" })
    }
}
