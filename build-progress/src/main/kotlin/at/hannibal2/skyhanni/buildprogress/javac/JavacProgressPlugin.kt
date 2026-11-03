package at.hannibal2.skyhanni.buildprogress.javac

import at.hannibal2.skyhanni.buildprogress.ProgressReport
import com.sun.source.util.JavacTask
import com.sun.source.util.Plugin
import com.sun.source.util.TaskEvent
import com.sun.source.util.TaskListener
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class JavacProgressPlugin : Plugin {

    override fun getName() = NAME

    override fun init(task: JavacTask, vararg args: String) {
        val reportFile = args.firstOrNull() ?: return
        task.addTaskListener(JavacProgressListener(ProgressReport(File(reportFile))))
    }

    companion object {
        const val NAME = "SkyHanniBuildProgress"
    }
}

class JavacProgressListener(private val report: ProgressReport) : TaskListener {

    private val parsedFiles = AtomicInteger()
    private val analyzedFiles = ConcurrentHashMap.newKeySet<String>()
    private val generatedFiles = ConcurrentHashMap.newKeySet<String>()

    override fun started(event: TaskEvent) {
        when (event.kind) {
            TaskEvent.Kind.ENTER -> report.phase("entering ${parsedFiles.get()} files")
            TaskEvent.Kind.ANALYZE -> step("analyzing", analyzedFiles, event)
            TaskEvent.Kind.GENERATE -> step("generating classes", generatedFiles, event)
            else -> Unit
        }
    }

    override fun finished(event: TaskEvent) {
        if (event.kind == TaskEvent.Kind.PARSE) report.step("parsing", parsedFiles.incrementAndGet(), 0, event.sourceFile?.name)
    }

    private fun step(verb: String, seen: MutableSet<String>, event: TaskEvent) {
        val name = event.sourceFile?.name ?: return
        seen.add(name)
        report.step(verb, seen.size, parsedFiles.get(), name)
    }
}
