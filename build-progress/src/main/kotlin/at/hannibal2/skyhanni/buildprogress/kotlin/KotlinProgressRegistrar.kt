package at.hannibal2.skyhanni.buildprogress.kotlin

import at.hannibal2.skyhanni.buildprogress.ProgressReport
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.jvm.extensions.ClassGeneratorExtension
import org.jetbrains.kotlin.cli.common.config.kotlinSourceRoots
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
class KotlinProgressRegistrar : CompilerPluginRegistrar() {

    override val pluginId = KotlinProgressOptions.PLUGIN_ID
    override val supportsK2 = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val reportFile = configuration.get(KotlinProgressOptions.REPORT_FILE_KEY) ?: return
        val tracker = KotlinProgressTracker(ProgressReport(File(reportFile))) { configuration.countKotlinSourceFiles() }
        tracker.compilationStarted()
        FirExtensionRegistrarAdapter.registerExtension(KotlinProgressFirRegistrar(tracker))
        IrGenerationExtension.registerExtension(KotlinProgressIrGenerationExtension(tracker))
        ClassGeneratorExtension.registerExtension(KotlinProgressClassGeneratorExtension(tracker))
    }

    private fun CompilerConfiguration.countKotlinSourceFiles() = kotlinSourceRoots.sumOf { root ->
        File(root.path).walk().count { it.isFile && it.extension in KOTLIN_EXTENSIONS }
    }

    companion object {
        private val KOTLIN_EXTENSIONS = setOf("kt", "kts")
    }
}
