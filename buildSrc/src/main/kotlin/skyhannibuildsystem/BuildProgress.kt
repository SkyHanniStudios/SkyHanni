package skyhannibuildsystem

import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.Internal
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.task.TaskFinishEvent
import skyhannibuildsystem.progress.DetektProgressListener
import skyhannibuildsystem.progress.JavacProgressPlugin
import skyhannibuildsystem.progress.KotlinProgressOptions
import skyhannibuildsystem.progress.PhaseTimer
import skyhannibuildsystem.progress.ProgressReport
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.milliseconds

interface ProgressLoggerFactoryAccessor {
    @get:Inject
    val factory: ProgressLoggerFactory
}

class BuildProgressWatcher(
    private val taskPath: String,
    private val reportFile: File,
    private val progressLogger: ProgressLogger,
    private val timingLogger: Logger?,
    private val initialStatus: String?,
) : AutoCloseable {

    private val lock = Any()
    private val timer = PhaseTimer()

    @Volatile
    private var open = true

    private val poller = thread(start = false, isDaemon = true, name = "build-progress ${reportFile.nameWithoutExtension}") {
        var last = ""
        while (open) {
            val status = runCatching { reportFile.takeIf(File::isFile)?.readText() }.getOrNull().orEmpty()
            if (status.isNotBlank() && status != last) {
                last = status
                synchronized(lock) {
                    if (!open) return@thread
                    timer.observe(status)
                    progressLogger.progress(status)
                }
            }
            runCatching { Thread.sleep(POLL_INTERVAL.inWholeMilliseconds) }.onFailure { return@thread }
        }
    }

    fun start() = apply {
        reportFile.delete()
        progressLogger.description = reportFile.nameWithoutExtension
        initialStatus?.let(progressLogger::started) ?: progressLogger.started()
        poller.start()
    }

    override fun close() {
        synchronized(lock) {
            if (!open) return
            open = false
            progressLogger.completed()
            timingLogger?.lifecycle("$taskPath  ${timer.stop()}")
        }
        poller.interrupt()
    }

    private companion object {
        val POLL_INTERVAL = 200.milliseconds
    }
}

abstract class BuildProgressService : BuildService<BuildServiceParameters.None>, OperationCompletionListener, AutoCloseable {

    private val watchers = ConcurrentHashMap<String, BuildProgressWatcher>()

    fun start(taskPath: String, watcher: BuildProgressWatcher) {
        watchers.put(taskPath, watcher.start())?.close()
    }

    fun finish(taskPath: String) {
        watchers.remove(taskPath)?.close()
    }

    override fun onFinish(event: FinishEvent) {
        (event as? TaskFinishEvent)?.descriptor?.taskPath?.let(::finish)
    }

    override fun close() = watchers.keys.toList().forEach(::finish)
}

class JavacProgressArgument(@get:Internal val reportFile: File) : CommandLineArgumentProvider {
    override fun asArguments() = listOf("-Xplugin:${JavacProgressPlugin.NAME} ${reportFile.absolutePath}")
}

object BuildProgress {
    const val DIRECTORY = ProgressReport.DIRECTORY
    const val KOTLIN_PLUGIN_ID = KotlinProgressOptions.PLUGIN_ID
    const val KOTLIN_REPORT_FILE_OPTION = KotlinProgressOptions.REPORT_FILE_OPTION
    const val DETEKT_REPORT_FILE_NAME = DetektProgressListener.REPORT_FILE_NAME
    const val TIMING_PROPERTY = "build.progress.log"

    fun watch(
        task: Task,
        reportFile: File,
        service: Provider<BuildProgressService>,
        objects: ObjectFactory,
        logTimings: Boolean,
        initialStatus: String? = null,
    ) {
        val taskPath = task.path
        task.usesService(service)
        task.doFirst {
            val factory = objects.newInstance(ProgressLoggerFactoryAccessor::class.java).factory
            val progressLogger = factory.newOperation(BuildProgressWatcher::class.java)
            val timingLogger = logger.takeIf { logTimings }
            service.get().start(taskPath, BuildProgressWatcher(taskPath, reportFile, progressLogger, timingLogger, initialStatus))
        }
        task.doLast { service.get().finish(taskPath) }
    }
}
