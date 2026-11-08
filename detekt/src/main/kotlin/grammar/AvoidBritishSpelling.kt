package grammar

import SkyHanniRule
import com.intellij.openapi.util.TextRange
import dev.detekt.api.Config
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

/**
 * This rule reports all usages of the british spelling over the american spelling in the codebase,
 * this will ignore any type annotations, i.e., `@ConfigEditorColour` will not be reported.
 */
@Suppress("AvoidBritishSpelling")
class AvoidBritishSpelling(config: Config) : SkyHanniRule(
    config,
    "String literals should use American spelling, such as color instead of colour.",
    CONVENTIONS_URI,
) {
    private data class BritishSpellingScan(
        val britishSpelling: String,
        val americanSpelling: String,
        val exceptions: Collection<Regex> = emptyList(),
    )

    private val scannedStrings = buildList {
        BritishSpellingScan(
            "colour",
            "color",
            buildList {
                add("^chromacolour$".toRegex(RegexOption.IGNORE_CASE))
            }
        ).let(::add)
        BritishSpellingScan("armour", "armor").let(::add)
    }

    override fun visitStringTemplateExpression(expression: KtStringTemplateExpression) {
        for (entry in expression.entries.filterIsInstance<KtLiteralStringTemplateEntry>()) {
            for ((british, american, exceptions) in scannedStrings) {
                var index = entry.text.indexOf(british, ignoreCase = true)
                while (index >= 0) {
                    if (exceptions.any { it.containsMatchIn(entry.text.wordAround(index, british.length)) }) {
                        index = entry.text.indexOf(british, index + british.length, ignoreCase = true)
                        continue
                    }
                    val start = entry.textRange.startOffset + index
                    val found = entry.text.substring(index, index + british.length)
                    entry.reportRange(
                        TextRange(start, start + british.length),
                        "Avoid using the word '$british' in code, '$american' is preferred instead.",
                        expression,
                    )
                    correct(entry, start, start + british.length, american.withCaseOf(found))
                    index = entry.text.indexOf(british, index + british.length, ignoreCase = true)
                }
            }
        }
        super.visitStringTemplateExpression(expression)
    }

    private fun String.wordAround(index: Int, length: Int): String {
        var start = index
        while (start > 0 && this[start - 1].isLetterOrDigit()) start--
        var end = index + length
        while (end < this.length && this[end].isLetterOrDigit()) end++
        return substring(start, end)
    }

    private fun String.withCaseOf(original: String): String = when {
        original.all { !it.isLetter() || it.isUpperCase() } -> uppercase()
        original.first().isUpperCase() -> replaceFirstChar(Char::uppercaseChar)
        else -> this
    }
}
