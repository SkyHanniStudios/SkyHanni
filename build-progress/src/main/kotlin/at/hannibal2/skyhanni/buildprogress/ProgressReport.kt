package at.hannibal2.skyhanni.buildprogress

import java.io.File
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds

class ProgressReport(private val reportFile: File) {

    private val lastWriteNanos = AtomicLong(Long.MIN_VALUE)

    fun phase(status: String) = write(status, force = true)

    fun step(verb: String, done: Int, total: Int, fileName: String?) {
        val count = if (total > 0) "$done/$total" else done.toString()
        val name = fileName?.substringAfterLast('/')?.substringAfterLast('\\').orEmpty()
        write("$verb $count $name".trimEnd())
    }

    private fun write(status: String, force: Boolean) {
        val now = System.nanoTime()
        val last = lastWriteNanos.get()
        if (!force && now - last < WRITE_INTERVAL_NANOS) return
        if (!lastWriteNanos.compareAndSet(last, now)) return
        runCatching {
            reportFile.parentFile?.mkdirs()
            reportFile.writeText(status)
        }
    }

    companion object {
        const val DIRECTORY = "build/build-progress"
        private val WRITE_INTERVAL_NANOS = 100.milliseconds.inWholeNanoseconds
    }
}
