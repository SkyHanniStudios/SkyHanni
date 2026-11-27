package skyhannibuildsystem.progress

import com.sun.source.util.JavacTask
import com.sun.source.util.Plugin
import com.sun.source.util.TaskEvent
import com.sun.source.util.TaskListener
import java.io.File

class JavacProgressPlugin : Plugin {

    override fun getName() = NAME

    override fun init(task: JavacTask, vararg args: String) {
        val reportFile = args.joinToString(" ").ifBlank { return }
        task.addTaskListener(JavacProgressProbe(ProgressReport(File(reportFile))))
    }

    private class JavacProgressProbe(report: ProgressReport) : ProgressProbe(report), TaskListener {

        private val parsed = FileSet(JavacHook.PARSE) { 0 }
        private val phases = TaskEvent.Kind.entries
            .mapNotNull { kind -> perFileHook(kind)?.let { kind to FileSet(it) { parsed.size } } }
            .toMap()

        init {
            entered(JavacHook.INIT)
        }

        override fun finished(event: TaskEvent) {
            if (event.kind == TaskEvent.Kind.COMPILATION) return entered(JavacHook.COMPILATION, parsed.size)
            val name = event.sourceFile?.name ?: return
            when (event.kind) {
                TaskEvent.Kind.PARSE -> parsed.add(name)
                else -> phases[event.kind]?.add(name)
            }
        }

        private fun perFileHook(kind: TaskEvent.Kind) = when (kind) {
            TaskEvent.Kind.ENTER -> JavacHook.ENTER
            TaskEvent.Kind.ANALYZE -> JavacHook.ANALYZE
            TaskEvent.Kind.GENERATE -> JavacHook.GENERATE
            else -> null
        }
    }

    companion object {
        const val NAME = "SkyHanniBuildProgress"
    }
}
