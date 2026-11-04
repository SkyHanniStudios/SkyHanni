package skyhannibuildsystem.progress

import dev.detekt.api.Detektion
import dev.detekt.api.FileProcessListener
import dev.detekt.api.Issue
import dev.detekt.api.SetupContext
import org.jetbrains.kotlin.psi.KtFile

class DetektProgressListener : FileProcessListener {

    override val id = "SkyHanniBuildProgress"

    private lateinit var probe: DetektProgressProbe

    override fun init(context: SetupContext) {
        val reportFile = context.basePath.resolve(ProgressReport.DIRECTORY).resolve(REPORT_FILE_NAME).toFile()
        probe = DetektProgressProbe(ProgressReport(reportFile))
    }

    override fun onStart(files: List<KtFile>) = probe.started(files.size)

    override fun onProcessComplete(file: KtFile, issues: List<Issue>) = probe.fileProcessed(file.virtualFilePath)

    override fun onFinish(files: List<KtFile>, result: Detektion): Detektion = result.also { probe.finished(files.size) }

    private class DetektProgressProbe(report: ProgressReport) : ProgressProbe(report) {

        @Volatile
        private var total = 0
        private val processed = Counter(DetektHook.PROCESS_COMPLETE) { total }

        init {
            entered(DetektHook.INIT)
        }

        fun started(fileCount: Int) {
            total = fileCount
            entered(DetektHook.START, fileCount)
        }

        fun fileProcessed(fileName: String) = processed.tick(fileName)

        fun finished(fileCount: Int) = entered(DetektHook.FINISH, fileCount)
    }

    companion object {
        const val REPORT_FILE_NAME = "detekt.txt"
    }
}
