package skyhannibuildsystem.progress

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.extensions.ClassGenerator
import org.jetbrains.kotlin.backend.jvm.extensions.ClassGeneratorExtension
import org.jetbrains.kotlin.cli.common.config.kotlinSourceRoots
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.fir.extensions.FirStatusTransformerExtension
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrTypeAlias
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.fileOrNull
import java.io.File
import java.nio.file.Files

object KotlinProgressOptions {
    const val PLUGIN_ID = "at.hannibal2.skyhanni.buildprogress"
    const val REPORT_FILE_OPTION = "reportFile"

    val REPORT_FILE = CliOption(
        optionName = REPORT_FILE_OPTION,
        valueDescription = "<path>",
        description = "File the compiler writes its current progress line to",
        required = true,
        allowMultipleOccurrences = false,
    )

    val REPORT_FILE_KEY = CompilerConfigurationKey<String>(REPORT_FILE_OPTION)
}

@OptIn(ExperimentalCompilerApi::class)
class KotlinProgressCommandLineProcessor : CommandLineProcessor {
    override val pluginId = KotlinProgressOptions.PLUGIN_ID
    override val pluginOptions = listOf(KotlinProgressOptions.REPORT_FILE)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) = when (option.optionName) {
        KotlinProgressOptions.REPORT_FILE_OPTION -> configuration.put(KotlinProgressOptions.REPORT_FILE_KEY, value)
        else -> throw CliOptionProcessingException("Unknown option ${option.optionName} for plugin $pluginId")
    }
}

class KotlinProgressProbe(report: ProgressReport, countSourceFiles: () -> Int) : ProgressProbe(report) {

    private val sourceFileCount by lazy(countSourceFiles)

    @Volatile
    private var classProducingFiles: Set<String> = emptySet()

    private val statusDeclarations = Counter(KotlinHook.STATUS) { 0 }
    private val checkedFiles = Counter(KotlinHook.CHECK) { sourceFileCount }
    private val generatedFiles = FileSet(KotlinHook.CODEGEN) { classProducingFiles.size }

    fun registered() = entered(KotlinHook.REGISTER)
    fun statusResolved() = statusDeclarations.tick()
    fun fileChecked(fileName: String) = checkedFiles.tick(fileName)

    fun irGenerated(fileCount: Int, classProducingFiles: Set<String>) {
        this.classProducingFiles = classProducingFiles
        entered(KotlinHook.IR, fileCount)
    }

    fun classGenerated(fileName: String?) {
        fileName?.takeIf(classProducingFiles::contains)?.let(generatedFiles::add)
    }
}

@OptIn(ExperimentalCompilerApi::class)
class KotlinProgressRegistrar : CompilerPluginRegistrar() {

    override val pluginId = KotlinProgressOptions.PLUGIN_ID
    override val supportsK2 = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val reportFile = configuration.get(KotlinProgressOptions.REPORT_FILE_KEY) ?: return
        val probe = KotlinProgressProbe(ProgressReport(File(reportFile))) { configuration.countKotlinSourceFiles() }
        probe.registered()
        FirExtensionRegistrarAdapter.registerExtension(Frontend(probe))
        IrGenerationExtension.registerExtension(Lowering(probe))
        ClassGeneratorExtension.registerExtension(Codegen(probe))
    }

    private fun CompilerConfiguration.countKotlinSourceFiles() = kotlinSourceRoots.sumOf { root ->
        File(root.path).walk().onEnter { !Files.isSymbolicLink(it.toPath()) }.count { it.isFile && it.extension in KOTLIN_EXTENSIONS }
    }

    private class Frontend(private val probe: KotlinProgressProbe) : FirExtensionRegistrar() {

        override fun ExtensionRegistrarContext.configurePlugin() {
            +{ session: FirSession -> StatusProbe(session, probe) }
            +{ session: FirSession -> Checkers(session, probe) }
        }

        private class StatusProbe(session: FirSession, private val probe: KotlinProgressProbe) : FirStatusTransformerExtension(session) {
            override fun needTransformStatus(declaration: FirDeclaration): Boolean {
                probe.statusResolved()
                return false
            }
        }

        private class Checkers(session: FirSession, probe: KotlinProgressProbe) : FirAdditionalCheckersExtension(session) {
            override val declarationCheckers = object : DeclarationCheckers() {
                override val fileCheckers = setOf(FileProbe(probe))
            }
        }

        private class FileProbe(private val probe: KotlinProgressProbe) : FirDeclarationChecker<FirFile>(MppCheckerKind.Common) {
            context(context: CheckerContext, reporter: DiagnosticReporter)
            override fun check(declaration: FirFile) = probe.fileChecked(declaration.name)
        }
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private class Lowering(private val probe: KotlinProgressProbe) : IrGenerationExtension {
        override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
            val files = moduleFragment.files
            val classProducing = files.asSequence()
                .filter { file -> file.declarations.any { it !is IrTypeAlias } }
                .map { it.fileEntry.name }
                .toSet()
            probe.irGenerated(files.size, classProducing)
        }
    }

    private class Codegen(private val probe: KotlinProgressProbe) : ClassGeneratorExtension {
        override fun generateClass(generator: ClassGenerator, declaration: IrClass?): ClassGenerator =
            generator.also { probe.classGenerated(declaration?.fileOrNull?.fileEntry?.name) }
    }

    private companion object {
        val KOTLIN_EXTENSIONS = setOf("kt", "kts")
    }
}
