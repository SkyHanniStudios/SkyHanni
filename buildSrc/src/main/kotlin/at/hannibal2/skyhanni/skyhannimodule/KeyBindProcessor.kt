package at.hannibal2.skyhanni.skyhannimodule

import at.hannibal2.skyhanni.utils.KeyBindCategory
import at.hannibal2.skyhanni.utils.KeyBindIds
import at.hannibal2.skyhanni.utils.KeyBindTrigger
import at.hannibal2.skyhanni.utils.RegisterKeyBind
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import java.io.OutputStreamWriter

@OptIn(KspExperimental::class)
class KeyBindProcessor(environment: SymbolProcessorEnvironment) : BaseProcessor(environment) {

    private class Entry(val property: KSPropertyDeclaration, val owner: String, val options: RegisterKeyBind) {
        val mappingKey = KeyBindIds.mappingKey(options.category, options.name)
    }

    override fun processSymbols(resolver: Resolver): List<KSAnnotated> {
        val entries = resolver.getSymbolsWithAnnotation(RegisterKeyBind::class.java.name)
            .filterIsInstance<KSPropertyDeclaration>()
            .mapNotNull(::validate)
            .sortedBy { it.mappingKey }
            .toList()

        entries.groupBy { it.mappingKey }.values.filter { it.size > 1 }.forEach { duplicates ->
            logger.error("Key bind id ${duplicates.first().mappingKey} is registered more than once", duplicates.last().property)
        }

        val sources = entries.mapNotNull { it.property.containingFile }.toTypedArray()
        val pages = entries.groupBy { it.options.category }.flatMap { (category, group) ->
            group.mapIndexed { index, entry -> entry to index / KeyBindIds.PAGE_SIZE }.map { (entry, page) -> Triple(entry, category, page) }
        }
        generateRegistry(pages, Dependencies(true, *sources))
        if (entries.isNotEmpty()) generateLang(pages, Dependencies(true, *sources))
        return emptyList()
    }

    private fun validate(property: KSPropertyDeclaration): Entry? {
        val options = property.getAnnotationsByType(RegisterKeyBind::class).first()
        val owner = (property.parentDeclaration as? KSClassDeclaration)?.takeIf { it.isModule() }?.qualifiedName?.asString()
        val error = when {
            owner == null -> "must be declared directly inside a class annotated with @SkyHanniModule, otherwise it is never registered"
            options.name.isBlank() -> "must have a non-blank name"
            else -> return Entry(property, owner, options)
        }
        logger.error("@${RegisterKeyBind::class.java.simpleName} property ${property.simpleName.asString()} $error", property)
        return null
    }

    private fun KSClassDeclaration.isModule(): Boolean =
        annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == SKYHANNI_MODULE }

    private fun generateRegistry(pages: List<Triple<Entry, KeyBindCategory, Int>>, dependencies: Dependencies) {
        val file = codeGenerator.createNewFile(dependencies, "at.hannibal2.skyhanni.utils", "LoadedKeyBinds")
        OutputStreamWriter(file).use { writer ->
            writer.appendLine("package at.hannibal2.skyhanni.utils")
            writer.appendLine()
            writer.appendLine("object LoadedKeyBinds {")
            writer.appendLine("    val registrations: List<KeyBindRegistration> = listOf(")
            for ((entry, category, page) in pages) {
                val context = entry.options.context
                writer.appendLine(
                    "        KeyBindRegistration(${entry.owner}::class.java, \"${entry.property.simpleName.asString()}\", " +
                        "\"${entry.mappingKey}\", \"${KeyBindIds.categoryPath(category, page)}\", " +
                        "inGame = ${context.inGame}, inScreen = ${context.inScreen}, hold = ${entry.options.trigger == KeyBindTrigger.HOLD}),",
                )
            }
            writer.appendLine("    )")
            writer.appendLine("}")
        }
        println("Generated LoadedKeyBinds file with ${pages.size} key binds")
    }

    private fun generateLang(pages: List<Triple<Entry, KeyBindCategory, Int>>, dependencies: Dependencies) {
        val translations = sortedMapOf<String, String>()
        for ((entry, category, page) in pages) {
            translations["key.category.skyhanni.${KeyBindIds.categoryPath(category, page)}"] = KeyBindIds.categoryLabel(category, page)
            translations[entry.mappingKey] = entry.options.name
        }
        val file = codeGenerator.createNewFile(dependencies, "assets.skyhanni.lang", "en_us", "json")
        OutputStreamWriter(file).use { writer ->
            writer.appendLine("{")
            writer.appendLine(translations.entries.joinToString(",\n") { (key, value) -> "    \"$key\": \"${value.escapeJson()}\"" })
            writer.appendLine("}")
        }
    }

    private fun String.escapeJson(): String = replace("\\", "\\\\").replace("\"", "\\\"")

    companion object {
        private const val SKYHANNI_MODULE = "at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule"
    }
}
