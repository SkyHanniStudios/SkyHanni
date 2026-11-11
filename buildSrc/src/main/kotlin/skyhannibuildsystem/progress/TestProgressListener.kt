package skyhannibuildsystem.progress

import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class TestProgressListener(private val reportFile: File) : TestListener {

    @Transient
    private var state: State? = null

    private class State(reportFile: File) {
        val report = ProgressReport(reportFile)
        val classesStarted = AtomicInteger()
        val testsCompleted = AtomicInteger()
    }

    private fun state() = state ?: State(reportFile).also { state = it }

    override fun beforeSuite(suite: TestDescriptor) {
        val className = suite.className ?: return
        state().run { report.step(TestHook.SUITE, classesStarted.incrementAndGet(), total = 0, className.simpleName()) }
    }

    override fun afterSuite(suite: TestDescriptor, result: TestResult) {
        if (suite.parent == null) state().report.enter(TestHook.FINISH, result.testCount.toInt())
    }

    override fun beforeTest(testDescriptor: TestDescriptor) = Unit

    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
        state().run { report.step(TestHook.TEST, testsCompleted.incrementAndGet(), total = 0, testDescriptor.className?.simpleName()) }
    }

    private fun String.simpleName() = substringAfterLast('.')
}
