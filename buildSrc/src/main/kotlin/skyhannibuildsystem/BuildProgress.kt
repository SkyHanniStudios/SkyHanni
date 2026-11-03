package skyhannibuildsystem

import org.gradle.api.Action
import org.gradle.api.Task
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
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

interface ProgressLoggerFactoryAccessor {
    @get:Inject
    val factory: ProgressLoggerFactory
}

class BuildProgressWatcher(private val reportFile: File, private val progressLogger: ProgressLogger) {

    private val thread = Thread(::poll, "build-progress ${reportFile.nameWithoutExtension}").apply { isDaemon = true }
    private val lock = Any()
    private var running = false

    fun start() {
        reportFile.delete()
        synchronized(lock) {
            progressLogger.start(reportFile.nameWithoutExtension, "starting")
            running = true
        }
        thread.start()
    }

    fun stop() {
        synchronized(lock) {
            if (!running) return
            running = false
            progressLogger.completed()
        }
        thread.interrupt()
    }

    private fun poll() {
        var lastStatus = ""
        while (true) {
            val status = runCatching { reportFile.takeIf { it.isFile }?.readText() }.getOrNull().orEmpty()
            if (status.isNotBlank() && status != lastStatus) {
                lastStatus = status
                synchronized(lock) {
                    if (!running) return
                    progressLogger.progress(status)
                }
            }
            try {
                Thread.sleep(POLL_INTERVAL.inWholeMilliseconds)
            } catch (e: InterruptedException) {
                return
            }
        }
    }

    companion object {
        private val POLL_INTERVAL = 200.milliseconds
    }
}

abstract class BuildProgressService : BuildService<BuildServiceParameters.None>, OperationCompletionListener, AutoCloseable {

    private val watchers = ConcurrentHashMap<String, BuildProgressWatcher>()

    fun start(taskPath: String, watcher: BuildProgressWatcher) {
        watchers.put(taskPath, watcher)?.stop()
        watcher.start()
    }

    fun finish(taskPath: String) {
        watchers.remove(taskPath)?.stop()
    }

    override fun onFinish(event: FinishEvent) {
        if (event is TaskFinishEvent) finish(event.descriptor.taskPath)
    }

    override fun close() {
        watchers.keys.toList().forEach(::finish)
    }
}

class JavacProgressArgument(@get:Internal val reportFile: File) : CommandLineArgumentProvider {
    override fun asArguments() = listOf("-Xplugin:${BuildProgress.JAVAC_PLUGIN_NAME} ${reportFile.absolutePath}")
}

object BuildProgress {
    const val DIRECTORY = "build/build-progress"
    const val KOTLIN_PLUGIN_ID = "at.hannibal2.skyhanni.buildprogress"
    const val KOTLIN_REPORT_FILE_OPTION = "reportFile"
    const val JAVAC_PLUGIN_NAME = "SkyHanniBuildProgress"
    const val DETEKT_REPORT_FILE_NAME = "detekt.txt"

    fun watch(task: Task, reportFile: File, service: Provider<BuildProgressService>, objects: ObjectFactory) {
        task.usesService(service)
        task.doFirst(StartWatcher(reportFile, service, objects, task.path))
        task.doLast(FinishWatcher(service, task.path))
    }

    private class StartWatcher(
        private val reportFile: File,
        private val service: Provider<BuildProgressService>,
        private val objects: ObjectFactory,
        private val taskPath: String,
    ) : Action<Task> {
        override fun execute(task: Task) {
            val factory = objects.newInstance(ProgressLoggerFactoryAccessor::class.java).factory
            val progressLogger = factory.newOperation(BuildProgressWatcher::class.java)
            service.get().start(taskPath, BuildProgressWatcher(reportFile, progressLogger))
        }
    }

    private class FinishWatcher(
        private val service: Provider<BuildProgressService>,
        private val taskPath: String,
    ) : Action<Task> {
        override fun execute(task: Task) = service.get().finish(taskPath)
    }
}
