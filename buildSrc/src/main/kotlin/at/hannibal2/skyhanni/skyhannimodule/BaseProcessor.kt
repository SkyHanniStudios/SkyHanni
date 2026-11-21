package at.hannibal2.skyhanni.skyhannimodule

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import skyhannibuildsystem.progress.KspProgressProbe

abstract class BaseProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {

    protected val codeGenerator: CodeGenerator = environment.codeGenerator
    protected val logger: KSPLogger = environment.logger
    protected val modVersion: String = environment.options[MOD_VERSION_OPTION] ?: UNVERSIONED
    protected val mcVersion: String = environment.options[MC_VERSION_OPTION] ?: DEFAULT_MC_VERSION
    protected val options: Map<String, String> = environment.options

    private val probe = KspProgressProbe.fromOptions(environment.options)?.also { it.created(name) }
    private val name get() = this::class.simpleName.orEmpty()
    private var processed = false

    final override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed || modVersion == UNVERSIONED) return emptyList()
        processed = true
        return processSymbols(resolver).also { probe?.processed(name) }
    }

    final override fun finish() = probe?.finished(name) ?: Unit

    protected abstract fun processSymbols(resolver: Resolver): List<KSAnnotated>

    companion object {
        const val MOD_VERSION_OPTION = "skyhanni.modver"
        const val MC_VERSION_OPTION = "skyhanni.mcver"
        const val BUILD_PATHS_OPTION = "skyhanni.buildpaths"
        const val CACHE_DIR_OPTION = "skyhanni.cachedir"
        private const val UNVERSIONED = "0.0.0"
        private const val DEFAULT_MC_VERSION = "26.3"
    }
}
