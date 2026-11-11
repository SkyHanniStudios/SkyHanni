package at.hannibal2.skyhanni.skyhannimodule

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStreamWriter

class PrimaryFunctionProcessor(environment: SymbolProcessorEnvironment) : BaseProcessor(environment) {

    override fun processSymbols(resolver: Resolver): List<KSAnnotated> {
        val skyHanniEvent = resolver.getClassDeclarationByName("at.hannibal2.skyhanni.api.event.SkyHanniEvent")
            ?.asStarProjectedType()
        if (skyHanniEvent == null) {
            logger.error("SkyHanniEvent could not be resolved, GeneratedEventPrimaryFunctionNames was not generated")
            return emptyList()
        }

        val symbols = resolver.getSymbolsWithAnnotation(PrimaryFunction::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .filter { skyHanniEvent.isAssignableFrom(it.asStarProjectedType()) }
            .toList()

        generate(symbols)
        return emptyList()
    }

    private fun generate(symbols: List<KSClassDeclaration>) {
        val entries = symbols.mapNotNull { symbol ->
            val annotation = symbol.annotations.firstOrNull { it.shortName.asString() == "PrimaryFunction" }
            val value = annotation?.arguments?.firstOrNull()?.value as? String ?: return@mapNotNull null
            val fqName = symbol.qualifiedName?.asString() ?: return@mapNotNull null
            "\"$value\" to $fqName::class.java"
        }.joinToString(",\n        ")

        val dependencies = Dependencies(true, *symbols.mapNotNull { it.containingFile }.toTypedArray())
        val file = codeGenerator.createNewFile(dependencies, "at.hannibal2.skyhanni.api.event", "GeneratedEventPrimaryFunctionNames")
        OutputStreamWriter(file).use {
            it.appendLine(
                """
                |package at.hannibal2.skyhanni.api.event
                |
                |object GeneratedEventPrimaryFunctionNames {
                |    val map: Map<String, Class<out SkyHanniEvent>> = mapOf(
                |        $entries
                |    )
                |}
                """.trimMargin(),
            )
        }
        println("Generated GeneratedEventPrimaryFunctionNames with ${symbols.size} entries")
    }
}
