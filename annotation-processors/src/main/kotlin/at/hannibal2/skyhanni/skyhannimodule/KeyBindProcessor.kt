package at.hannibal2.skyhanni.skyhannimodule

import at.hannibal2.skyhanni.utils.KeyBindIds
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

    private class Entry(val property: KSPropertyDeclaration, val options: RegisterKeyBind) {
        val translationKey = KeyBindIds.mappingKey(options.category, options.name)
    }

    override fun processSymbols(resolver: Resolver): List<KSAnnotated> {
        val entries = resolver.getSymbolsWithAnnotation(RegisterKeyBind::class.java.name)
            .filterIsInstance<KSPropertyDeclaration>()
            .mapNotNull(::validate).toList()

        entries.groupBy { it.translationKey }.values.filter { it.size > 1 }.forEach { duplicates ->
            logger.error("Key bind id ${duplicates.first().translationKey} is registered more than once", duplicates.last().property)
        }
        if (entries.isNotEmpty()) generateLang(entries)
        return emptyList()
    }

    private fun validate(property: KSPropertyDeclaration): Entry? {
        val options = property.getAnnotationsByType(RegisterKeyBind::class).first()
        val owner = property.parentDeclaration as? KSClassDeclaration
        val error = when {
            owner == null || !owner.isModule() ->
                "must be declared directly inside a class annotated with @SkyHanniModule, otherwise it is never registered"

            options.name.isBlank() -> "must have a non-blank name"
            else -> return Entry(property, options)
        }
        logger.error("@${RegisterKeyBind::class.java.simpleName} property ${property.simpleName.asString()} $error", property)
        return null
    }

    private fun KSClassDeclaration.isModule(): Boolean =
        annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == SKYHANNI_MODULE }

    private fun generateLang(entries: List<Entry>) {
        val translations = sortedMapOf<String, String>()
        for ((category, group) in entries.groupBy { it.options.category }) {
            repeat((group.size + KeyBindIds.PAGE_SIZE - 1) / KeyBindIds.PAGE_SIZE) { page ->
                translations[KeyBindIds.categoryKey(category, page)] = KeyBindIds.categoryLabel(category, page)
            }
            group.forEach { translations[it.translationKey] = it.options.name }
        }

        val sources = entries.mapNotNull { it.property.containingFile }.toTypedArray()
        val file = codeGenerator.createNewFile(Dependencies(true, *sources), "assets.skyhanni.lang", "en_us", "json")
        OutputStreamWriter(file).use { writer ->
            writer.appendLine("{")
            writer.appendLine(translations.entries.joinToString(",\n") { (key, value) -> "    \"$key\": \"${value.escapeJson()}\"" })
            writer.appendLine("}")
        }
        println("Generated key bind translations for ${entries.size} key binds")
    }

    private fun String.escapeJson(): String = replace("\\", "\\\\").replace("\"", "\\\"")

    companion object {
        private const val SKYHANNI_MODULE = "at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule"
    }
}
