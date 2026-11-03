package at.hannibal2.skyhanni.buildprogress.kotlin

import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@OptIn(ExperimentalCompilerApi::class)
object KotlinProgressOptions {
    const val PLUGIN_ID = "at.hannibal2.skyhanni.buildprogress"

    val REPORT_FILE = CliOption(
        optionName = "reportFile",
        valueDescription = "<path>",
        description = "File the compiler writes its current progress line to",
        required = true,
        allowMultipleOccurrences = false,
    )

    val REPORT_FILE_KEY = CompilerConfigurationKey<String>(REPORT_FILE.optionName)
}
