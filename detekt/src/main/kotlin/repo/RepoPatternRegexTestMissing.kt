package repo

import SkyHanniRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.psi.KtPropertyDelegate

class RepoPatternRegexTestMissing(config: Config, private val ctx: RepoPatternContext) : SkyHanniRule(
    config,
    "Repo patterns that use regex syntax should have at least one REGEX-TEST line in their KDoc.",
    CONVENTIONS_URI,
) {

    override fun visitPropertyDelegate(delegate: KtPropertyDelegate) {
        super.visitPropertyDelegate(delegate)

        val repoPatternElement = ctx.getRepoPatternElement(delegate) ?: return
        val variableName = repoPatternElement.variableName
        val rawPattern = repoPatternElement.rawPattern

        if (!rawPattern.needsRegexTest()) return

        if (repoPatternElement.regexTests.isEmpty()) {
            (repoPatternElement.nameElement ?: delegate).reportIssue("Repo pattern `$variableName` must have a regex test.", delegate)
            return
        }
    }

    private fun String.needsRegexTest(): Boolean {
        return regexConstructs.containsMatchIn(this)
    }

    companion object {
        val regexConstructs = Regex("""(?<!\\)[.*+(){}\[|?]""")
    }
}
