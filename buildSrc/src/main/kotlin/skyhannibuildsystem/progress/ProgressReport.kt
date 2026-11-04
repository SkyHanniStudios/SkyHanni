package skyhannibuildsystem.progress

import java.io.File
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.milliseconds

class ProgressReport(private val reportFile: File) {

    sealed interface Status {
        val hook: ProgressHook
        val isTerminal: Boolean

        data class Entered(override val hook: ProgressHook, val fileCount: Int = 0) : Status {
            override val isTerminal get() = true
        }

        data class Stepped(override val hook: ProgressHook, val done: Int, val total: Int, val fileName: String?) : Status {
            override val isTerminal get() = total in 1..done
        }
    }

    private val lastWriteNanos = AtomicLong(System.nanoTime() - WRITE_INTERVAL_NANOS)
    private val written = AtomicReference<Status>()

    fun enter(hook: ProgressHook, fileCount: Int = 0) = publish(Status.Entered(hook, fileCount))

    fun step(hook: ProgressHook, done: Int, total: Int, fileName: String? = null) =
        publish(Status.Stepped(hook, done, total, fileName))

    private fun publish(status: Status) {
        if (!status.isTerminal && !claimWriteSlot()) return
        val previous = written.get()
        if (status.regresses(previous) || !written.compareAndSet(previous, status)) return
        if (status.isTerminal) lastWriteNanos.set(System.nanoTime())
        runCatching {
            reportFile.parentFile?.mkdirs()
            reportFile.writeText(status.render())
        }
    }

    private fun Status.regresses(previous: Status?) =
        this is Status.Stepped && previous is Status.Stepped && previous.hook == hook && done < previous.done

    private fun claimWriteSlot(): Boolean {
        val now = System.nanoTime()
        val last = lastWriteNanos.get()
        return now - last >= WRITE_INTERVAL_NANOS && lastWriteNanos.compareAndSet(last, now)
    }

    private fun Status.render(): String = when (this) {
        is Status.Entered -> if (fileCount > 0) "${hook.id} $fileCount/$fileCount" else hook.id
        is Status.Stepped -> buildString {
            append(hook.id).append(' ')
            append(if (total > 0) "$done/$total" else done)
            fileName?.substringAfterLast('/')?.substringAfterLast('\\')?.let { append(' ').append(it) }
        }
    }

    companion object {
        const val DIRECTORY = "build/build-progress"
        private val WRITE_INTERVAL_NANOS = 100.milliseconds.inWholeNanoseconds
    }
}
