package repo

import SkyHanniRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.psi.KtPropertyDelegate

class RepoPatternRegexTestFailed(config: Config, private val ctx: RepoPatternContext) : SkyHanniRule(
    config,
    "Every REGEX-TEST line of a repo pattern should match it, and every REGEX-FAIL line should not.",
    CONVENTIONS_URI,
) {

    override fun visitPropertyDelegate(delegate: KtPropertyDelegate) {
        super.visitPropertyDelegate(delegate)

        val repoPatternElement = ctx.getRepoPatternElement(delegate) ?: return
        val variableName = repoPatternElement.variableName
        val rawPattern = repoPatternElement.rawPattern
        val docTarget = repoPatternElement.docElement ?: delegate
        val patternTarget = repoPatternElement.patternElement ?: delegate

        repoPatternElement.kDocErrors.forEach { error ->
            docTarget.reportIssue("Repo pattern `$variableName`: $error", delegate)
        }

        if (!rawPattern.needsRegexTest()) return

        if (repoPatternElement.regexTests.isEmpty()) return

        val pattern = repoPatternElement.pattern
        if (pattern == null) {
            patternTarget.reportIssue("Repo pattern `$variableName` is not a valid regex: `$rawPattern`", delegate)
            return
        }

        repoPatternElement.regexTests.forEach { test ->
            if (!pattern.matcher(test).find()) {
                patternTarget.reportIssue(
                    "Repo pattern `$variableName` failed regex test: `$test` pattern: `$rawPattern`. " +
                        "[View on Regex101](${repoPatternElement.regex101Url})",
                    delegate,
                )
            }
        }

        repoPatternElement.failingRegexTests.forEach { test ->
            if (pattern.matcher(test).find()) {
                patternTarget.reportIssue(
                    "Repo pattern `$variableName` passed regex test: `$test` pattern: `$rawPattern` " +
                        "even though it was set to fail. [View on Regex101](${repoPatternElement.regex101Url})",
                    delegate,
                )
            }
        }
    }

    private fun String.needsRegexTest(): Boolean {
        return regexConstructs.containsMatchIn(this)
    }

    companion object {
        val regexConstructs = Regex("""(?<!\\)[.*+(){}\[|?]""")
    }
}
