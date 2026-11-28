package at.hannibal2.skyhanni.skyhannimodule

import at.hannibal2.skyhanni.utils.KeyBindCategory
import at.hannibal2.skyhanni.utils.KeyBindContext
import at.hannibal2.skyhanni.utils.KeyBindIds
import at.hannibal2.skyhanni.utils.KeyBindTrigger
import at.hannibal2.skyhanni.utils.RegisterKeyBind
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isPrivate
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

        val dependencies = Dependencies(true, *entries.mapNotNull { it.property.containingFile }.toTypedArray())
        generateRegistry(entries, dependencies)
        if (entries.isNotEmpty()) generateLang(entries, dependencies)
        return emptyList()
    }

    private fun validate(property: KSPropertyDeclaration): Entry? {
        val options = property.getAnnotationsByType(RegisterKeyBind::class).first()
        val owner = (property.parentDeclaration as? KSClassDeclaration)?.takeIf { it.isModule() }?.qualifiedName?.asString()
        val error = when {
            owner == null -> "must be declared directly inside a class annotated with @SkyHanniModule, otherwise it is never registered"
            property.isPrivate() -> "must be internal, the generated LoadedKeyBinds references it directly"
            options.name.isBlank() -> "must have a non-blank name"
            else -> return Entry(property, owner, options)
        }
        logger.error("@${RegisterKeyBind::class.java.simpleName} property ${property.simpleName.asString()} $error", property)
        return null
    }

    private fun KSClassDeclaration.isModule(): Boolean =
        annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == SKYHANNI_MODULE }

    private fun generateRegistry(entries: List<Entry>, dependencies: Dependencies) {
        val annotation = RegisterKeyBind::class.java.simpleName
        val file = codeGenerator.createNewFile(dependencies, RegisterKeyBind::class.java.packageName, "LoadedKeyBinds")
        OutputStreamWriter(file).use { writer ->
            writer.appendLine("package ${RegisterKeyBind::class.java.packageName}")
            writer.appendLine()
            writer.appendLine("object LoadedKeyBinds {")
            writer.appendLine("    val keyBinds: List<Pair<$annotation, SkyHanniKeyBind>> = listOf(")
            for (entry in entries) {
                val options = entry.options
                writer.appendLine(
                    "        $annotation(name = \"${options.name.escape().replace("$", "\\$")}\", " +
                        "category = ${KeyBindCategory::class.java.simpleName}.${options.category.name}, " +
                        "context = ${KeyBindContext::class.java.simpleName}.${options.context.name}, " +
                        "trigger = ${KeyBindTrigger::class.java.simpleName}.${options.trigger.name}) " +
                        "to ${entry.owner}.${entry.property.simpleName.asString()},",
                )
            }
            writer.appendLine("    )")
            writer.appendLine("}")
        }
        println("Generated LoadedKeyBinds file with ${entries.size} key binds")
    }

    private fun generateLang(entries: List<Entry>, dependencies: Dependencies) {
        val translations = sortedMapOf<String, String>()
        for ((category, group) in entries.groupBy { it.options.category }) {
            group.forEachIndexed { index, entry ->
                val page = index / KeyBindIds.PAGE_SIZE
                translations["key.category.skyhanni.${KeyBindIds.categoryPath(category, page)}"] = KeyBindIds.categoryLabel(category, page)
                translations[entry.mappingKey] = entry.options.name
            }
        }
        val file = codeGenerator.createNewFile(dependencies, "assets.skyhanni.lang", "en_us", "json")
        OutputStreamWriter(file).use { writer ->
            writer.appendLine("{")
            writer.appendLine(translations.entries.joinToString(",\n") { (key, value) -> "    \"$key\": \"${value.escape()}\"" })
            writer.appendLine("}")
        }
    }

    private fun String.escape(): String = replace("\\", "\\\\").replace("\"", "\\\"")

    companion object {
        private const val SKYHANNI_MODULE = "at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule"
    }
}
