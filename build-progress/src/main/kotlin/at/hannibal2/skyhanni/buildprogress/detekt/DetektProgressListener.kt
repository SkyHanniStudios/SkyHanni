package at.hannibal2.skyhanni.buildprogress.detekt

import at.hannibal2.skyhanni.buildprogress.ProgressReport
import dev.detekt.api.Detektion
import dev.detekt.api.FileProcessListener
import dev.detekt.api.Issue
import dev.detekt.api.SetupContext
import org.jetbrains.kotlin.psi.KtFile
import java.util.concurrent.atomic.AtomicInteger

class DetektProgressListener : FileProcessListener {

    override val id = "SkyHanniBuildProgress"

    private var report: ProgressReport? = null
    private val processedFiles = AtomicInteger()

    @Volatile
    private var totalFiles = 0

    override fun init(context: SetupContext) {
        report = ProgressReport(context.basePath.resolve(ProgressReport.DIRECTORY).resolve(REPORT_FILE_NAME).toFile())
    }

    override fun onStart(files: List<KtFile>) {
        totalFiles = files.size
        report?.phase("analyzing ${files.size} files")
    }

    override fun onProcessComplete(file: KtFile, issues: List<Issue>) {
        report?.step("checking", processedFiles.incrementAndGet(), totalFiles, file.virtualFilePath)
    }

    override fun onFinish(files: List<KtFile>, result: Detektion): Detektion {
        report?.phase("writing reports")
        return result
    }

    companion object {
        const val REPORT_FILE_NAME = "detekt.txt"
    }
}
