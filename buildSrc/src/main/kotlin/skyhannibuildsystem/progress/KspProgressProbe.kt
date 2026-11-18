package skyhannibuildsystem.progress

import java.io.File
import java.util.concurrent.ConcurrentHashMap

class KspProgressProbe private constructor(report: ProgressReport, private val processorCount: Int) : ProgressProbe(report) {

    private val created = Counter(KspHook.CREATE) { processorCount }
    private val processed = Counter(KspHook.PROCESS) { processorCount }
    private val finished = Counter(KspHook.FINISH) { processorCount }

    fun created(processor: String) = created.tick(processor)
    fun processed(processor: String) = processed.tick(processor)
    fun finished(processor: String) = finished.tick(processor)

    companion object {
        const val REPORT_FILE_OPTION = "skyhanni.progressfile"
        const val PROCESSOR_COUNT_OPTION = "skyhanni.processorcount"

        private val shared = ConcurrentHashMap<String, KspProgressProbe>()

        fun fromOptions(options: Map<String, String>): KspProgressProbe? {
            val reportFile = options[REPORT_FILE_OPTION] ?: return null
            val processorCount = options[PROCESSOR_COUNT_OPTION]?.toIntOrNull() ?: 0
            return shared.getOrPut(reportFile) { KspProgressProbe(ProgressReport(File(reportFile)), processorCount) }
        }
    }
}
