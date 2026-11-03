package at.hannibal2.skyhanni.buildprogress.kotlin

import at.hannibal2.skyhanni.buildprogress.ProgressReport
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class KotlinProgressTracker(private val report: ProgressReport, countSourceFiles: () -> Int) {

    private val sourceFileCount by lazy(countSourceFiles)
    private val checkedFiles = AtomicInteger()
    private val generatedFiles = ConcurrentHashMap.newKeySet<String>()

    @Volatile
    private var moduleFileCount = 0

    fun compilationStarted() = report.phase("analyzing")

    fun fileChecked(fileName: String) = report.step("checking", checkedFiles.incrementAndGet(), sourceFileCount, fileName)

    fun loweringStarted(fileCount: Int) {
        moduleFileCount = fileCount
        report.phase("lowering $fileCount files")
    }

    fun classGenerated(fileName: String?) {
        val name = fileName ?: return
        generatedFiles.add(name)
        report.step("generating classes", generatedFiles.size, moduleFileCount, name)
    }
}
