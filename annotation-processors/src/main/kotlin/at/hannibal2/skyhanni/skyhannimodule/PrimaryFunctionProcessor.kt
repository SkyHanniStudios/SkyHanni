package at.hannibal2.skyhanni.skyhannimodule

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStreamWriter

private const val SKY_HANNI_EVENT = "at.hannibal2.skyhanni.api.event.SkyHanniEvent"

class PrimaryFunctionProcessor(
    codeGenerator: CodeGenerator,
    logger: KSPLogger,
    modVersion: String,
    mcVersion: String,
    cacheDir: String?,
) : BaseProcessor(codeGenerator, logger, modVersion) {

    private val cache = KspIncrementalCache(cacheDir, mcVersion, "ksp-primary-function-state")

    override fun processSymbols(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(PrimaryFunction::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.extendsSkyHanniEvent() }
            .toList()

        val filePaths = symbols.mapNotNull { it.containingFile?.filePath }.toSet()
        val outputFile = cache.outputFile("at/hannibal2/skyhanni/api/event", "GeneratedEventPrimaryFunctionNames")
        val dirtyFilePaths = cache.evaluate(filePaths, outputFile)

        if (dirtyFilePaths == null) {
            logger.warn("No @PrimaryFunction files changed, skipping GeneratedEventPrimaryFunctionNames regeneration")
            cache.commit()
            return emptyList()
        }

        if (dirtyFilePaths.isEmpty()) {
            logger.warn("No @PrimaryFunction files changed but GeneratedEventPrimaryFunctionNames.kt is missing, regenerating")
        }

        generate(symbols)
        cache.commit()
        return emptyList()
    }

    private fun KSClassDeclaration.extendsSkyHanniEvent(): Boolean = qualifiedName?.asString() == SKY_HANNI_EVENT ||
        superTypes.any { (it.resolve().declaration as? KSClassDeclaration)?.extendsSkyHanniEvent() == true }

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
