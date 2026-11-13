package skyhannibuildsystem.progress

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

abstract class ProgressProbe(protected val report: ProgressReport) {

    protected fun entered(hook: ProgressHook, fileCount: Int = 0) = report.enter(hook, fileCount)

    protected inner class Counter(private val hook: ProgressHook, private val total: () -> Int) {
        private val count = AtomicInteger()

        fun tick(fileName: String? = null) = report.step(hook, count.incrementAndGet(), total(), fileName)
    }

    protected inner class FileSet(private val hook: ProgressHook, private val total: () -> Int) {
        private val seen = ConcurrentHashMap.newKeySet<String>()
        val size get() = seen.size

        fun add(fileName: String) {
            seen += fileName
            report.step(hook, seen.size, total(), fileName)
        }
    }
}
