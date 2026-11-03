package at.hannibal2.skyhanni.buildprogress.kotlin

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class KotlinProgressCommandLineProcessor : CommandLineProcessor {
    override val pluginId = KotlinProgressOptions.PLUGIN_ID
    override val pluginOptions = listOf(KotlinProgressOptions.REPORT_FILE)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            KotlinProgressOptions.REPORT_FILE.optionName -> configuration.put(KotlinProgressOptions.REPORT_FILE_KEY, value)
            else -> throw CliOptionProcessingException("Unknown option ${option.optionName} for plugin $pluginId")
        }
    }
}
