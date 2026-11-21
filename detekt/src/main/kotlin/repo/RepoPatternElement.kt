package repo

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyDelegate
import org.jetbrains.kotlin.psi.KtStringTemplateEntryWithExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import java.net.URLEncoder

class RepoPatternElement private constructor(
    val variableName: String,
    val rawKey: String?,
    val rawPattern: String,
    val regexTests: List<String>,
    val failingRegexTests: List<String>,
    val kDocErrors: List<String>,
) {

    val pattern by lazy { rawPattern.toPattern() }

    val regex101Url: String by lazy {
        val encodedPattern = URLEncoder.encode(rawPattern.replace("\"", "\\\""), "UTF-8")
        val urlEncodedNewLine = URLEncoder.encode("\n", "UTF-8")
        val encodedTests = regexTests.joinToString(urlEncodedNewLine) { URLEncoder.encode(it, "UTF-8") }
        "https://regex101.com/?regex=$encodedPattern&testString=$encodedTests&flavor=java"
    }

    companion object {
        // A sentinel value to indicate that a KtPropertyDelegate has been processed but does not correspond to a valid RepoPatternElement.
        // Must be internal so that it can be accessed from RepoPatternContext.
        internal val SENTINEL_VALUE = RepoPatternElement("SENTINEL", null, "", emptyList(), emptyList(), emptyList())

        private val wrappedRegexTestPattern = "@regexTestWrapped \"(?<test>.*)\"".toPattern()
        private val wrappedRegexFailPattern = "@regexFailWrapped \"(?<test>.*)\"".toPattern()
        private val kDocLinePrefixPattern = "^\\s*\\* ?".toPattern()

        private fun kDocLines(text: String): List<String> {
            val body = text.removePrefix("/**").removeSuffix("*/")
            if ('\n' !in body) return listOf(body.trim())
            return body.lines().map { kDocLinePrefixPattern.matcher(it).replaceFirst("") }
        }

        internal fun KtPropertyDelegate.asRepoPatternElement(): RepoPatternElement? {
            val expression = this.expression as? KtDotQualifiedExpression ?: return null
            val callExpression = expression.selectorExpression as? KtCallExpression ?: return null
            if (callExpression.valueArguments.size != 2) return null

            val patternKey = callExpression.valueArguments[0].getArgumentExpression() ?: return null
            val patternArg = callExpression.valueArguments[1].getArgumentExpression() ?: return null

            val rawKey = patternKey.asPlainStringOrNull()
            // We only want to match on plain pattern strings, not string templates
            val rawPattern = patternArg.asPlainStringOrNull() ?: return null

            val parent = parent as? KtProperty ?: return null
            val variableName = parent.name ?: "unknownPattern"

            val (regexTests, failingRegexTests, kDocErrors) = findRegexTestInKDoc(parent)
            return RepoPatternElement(variableName, rawKey, rawPattern, regexTests, failingRegexTests, kDocErrors)
        }

        private fun KtExpression.asPlainStringOrNull(): String? {
            val template = this as? KtStringTemplateExpression ?: return null
            if (template.entries.any { it is KtStringTemplateEntryWithExpression }) return null

            return template.entries.joinToString("") { entry ->
                when (entry) {
                    is KtLiteralStringTemplateEntry -> entry.text
                    is KtEscapeStringTemplateEntry -> entry.unescapedValue
                    else -> ""
                }
            }.removeSurrounding("\"")
                .replace("\n", "")
        }

        private fun findRegexTestInKDoc(property: KtProperty): Triple<List<String>, List<String>, List<String>> {
            val kDoc = property.docComment ?: return Triple(emptyList(), emptyList(), emptyList())

            val regexTests = mutableListOf<String>()
            val failingRegexTests = mutableListOf<String>()
            val kDocErrors = mutableListOf<String>()

            kDocLines(kDoc.text).forEach { line ->
                wrappedRegexTestPattern.matcher(line).let { matcher ->
                    if (!matcher.find()) return@let
                    val test = matcher.group("test") ?: return@let
                    regexTests.add(test)
                    return@forEach
                }
                wrappedRegexFailPattern.matcher(line).let { matcher ->
                    if (!matcher.find()) return@let
                    val test = matcher.group("test") ?: return@let
                    failingRegexTests.add(test)
                    return@forEach
                }
                if (line.contains("@regexTest ")) {
                    val test = line.substringAfter("@regexTest ")
                    if (test.trim() != test) {
                        kDocErrors.add(
                            "Plain @regexTest must not contain leading or trailing whitespace. " +
                                "If the whitespace is intentional, use @regexTestWrapped instead.",
                        )
                    } else {
                        regexTests.add(test)
                    }
                    return@forEach
                }
                if (line.contains("@regexFail ")) {
                    val test = line.substringAfter("@regexFail ")
                    if (test.trim() != test) {
                        kDocErrors.add(
                            "Plain @regexFail must not contain leading or trailing whitespace. " +
                                "If the whitespace is intentional, use @regexFailWrapped instead.",
                        )
                    } else {
                        failingRegexTests.add(test)
                    }
                    return@forEach
                }
            }
            return Triple(regexTests, failingRegexTests, kDocErrors)
        }
    }
}
