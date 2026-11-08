package repo

import SkyHanniRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtPropertyDelegate
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

class RepoPatternUnnamedGroup(config: Config, private val ctx: RepoPatternContext) : SkyHanniRule(
    config,
    "Repo patterns should use named or non-capturing groups instead of unnamed capture groups.",
    CONVENTIONS_URI,
) {

    override fun visitPropertyDelegate(delegate: KtPropertyDelegate) {
        super.visitPropertyDelegate(delegate)

        val repoPatternElement = ctx.getRepoPatternElement(delegate) ?: return

        if (repoPatternElement.rawPattern.hasUnnamedGroup()) {
            val patternElement = repoPatternElement.patternElement
            (patternElement ?: delegate).reportIssue(
                "Repo pattern `${repoPatternElement.variableName}` must not contain unnamed capture groups.",
                delegate,
            )
            (patternElement as? KtStringTemplateExpression)?.let(::makeGroupsNonCapturing)
        }
    }

    private fun makeGroupsNonCapturing(template: KtStringTemplateExpression) {
        if (!canCorrect(template)) return
        val chars = mutableListOf<Pair<Char, Int>>()
        for (entry in template.entries) {
            when (entry) {
                is KtLiteralStringTemplateEntry -> entry.text.forEachIndexed { index, char ->
                    if (char != '\n') chars += char to entry.textRange.startOffset + index
                }
                is KtEscapeStringTemplateEntry -> entry.unescapedValue.forEach { chars += it to entry.textRange.startOffset }
                else -> return
            }
        }
        var index = 0
        var inClass = false
        while (index < chars.size) {
            val (char, offset) = chars[index]
            when {
                char == '\\' -> index++
                inClass -> if (char == ']') inClass = false
                char == '[' -> {
                    inClass = true
                    if (chars.getOrNull(index + 1)?.first == '^') index++
                    if (chars.getOrNull(index + 1)?.first == ']') index++
                }
                char == '(' && chars.getOrNull(index + 1)?.first != '?' -> correct(template, offset + 1, offset + 1, "?:")
            }
            index++
        }
    }

    private fun String.hasUnnamedGroup(): Boolean {
        // Remove content inside square brackets
        val withoutSquareBrackets = squareBracketRegex.replace(this, "")
        // Check if simplified string contains unnamed groups
        return unnamedGroupRegex.containsMatchIn(withoutSquareBrackets)
    }

    companion object {
        // Regex to find content inside square brackets, including nested brackets
        private val squareBracketRegex = Regex("""(?<!\\)\[(?:\^])?(?:\\.|[^]])*]""")
        // Regex to find unescaped '(' not followed by '?'
        private val unnamedGroupRegex = Regex("""(?<!\\)\((?!\?)""")
    }
}
