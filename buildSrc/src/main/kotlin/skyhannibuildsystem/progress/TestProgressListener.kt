package skyhannibuildsystem.progress

import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class TestProgressListener(private val reportFile: File) : TestListener {

    @Transient
    private var report: ProgressReport? = null

    @Transient
    private var completed: AtomicInteger? = null

    private fun report() = report ?: ProgressReport(reportFile).also { report = it }

    private fun completed() = completed ?: AtomicInteger().also { completed = it }

    override fun beforeSuite(suite: TestDescriptor) {
        if (suite.parent == null) report().enter(TestHook.SUITE)
    }

    override fun afterSuite(suite: TestDescriptor, result: TestResult) {
        if (suite.parent == null) report().enter(TestHook.FINISH, result.testCount.toInt())
    }

    override fun beforeTest(testDescriptor: TestDescriptor) = Unit

    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
        report().step(TestHook.TEST, completed().incrementAndGet(), total = 0, testDescriptor.className?.substringAfterLast('.'))
    }
}
