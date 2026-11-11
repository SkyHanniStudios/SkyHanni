package at.hannibal2.skyhanni.skyhannimodule

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class PrimaryFunctionProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = PrimaryFunctionProcessor(environment)
}
