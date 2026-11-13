package repo

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class RepoRuleSetProvider : SkyHanniRuleSetProvider("RepoRules") {
    private val repoContext = RepoPatternContext()

    init {
        rule(::SkullTexturesUseRepo)
        rule { RepoPatternRegexTestFailed(it, repoContext) }
        rule { RepoPatternRegexTestMissing(it, repoContext) }
        rule { RepoPatternUnnamedGroup(it, repoContext) }
        rule { RepoPatternPassesShapeRequirement(it, repoContext) }
    }
}
