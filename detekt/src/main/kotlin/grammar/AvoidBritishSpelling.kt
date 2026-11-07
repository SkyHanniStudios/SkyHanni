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
class AvoidBritishSpelling(config: Config) : SkyHanniRule(
    config,
    "String literals should use American spelling, such as color instead of colour.",
    CONVENTIONS_URI,
) {

    private val scannedWords = mapOf(
        "colour" to "color",
        "armour" to "armor",
    )

    override fun visitStringTemplateExpression(expression: KtStringTemplateExpression) {
        for (entry in expression.entries.filterIsInstance<KtLiteralStringTemplateEntry>()) {
            for ((british, american) in scannedWords) {
                var index = entry.text.indexOf(british, ignoreCase = true)
                while (index >= 0) {
                    val start = entry.textRange.startOffset + index
                    entry.reportRange(
                        TextRange(start, start + british.length),
                        "Avoid using the word '$british' in code, '$american' is preferred instead.",
                        expression,
                    )
                    index = entry.text.indexOf(british, index + british.length, ignoreCase = true)
                }
            }
        }
        super.visitStringTemplateExpression(expression)
    }
}
