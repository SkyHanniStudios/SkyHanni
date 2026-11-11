package at.hannibal2.skyhanni.skyhannimodule

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class VersionConstantsProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = VersionConstantsProcessor(environment)
}
