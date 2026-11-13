package repo

import com.google.auto.service.AutoService
import dev.detekt.api.RuleName
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class RepoRuleSetProvider : RuleSetProvider {
    override val ruleSetId: RuleSetId = RuleSetId("RepoRules")
    private val repoContext = RepoPatternContext()

    override fun instance(): RuleSet = RuleSet(
        ruleSetId,
        mapOf(
            RuleName("SkullTexturesUseRepo") to ::SkullTexturesUseRepo,
            RuleName("RepoPatternRegexTestFailed") to { config -> RepoPatternRegexTestFailed(config, repoContext) },
            RuleName("RepoPatternRegexTestMissing") to { config -> RepoPatternRegexTestMissing(config, repoContext) },
            RuleName("RepoPatternUnnamedGroup") to { config -> RepoPatternUnnamedGroup(config, repoContext) },
            RuleName("RepoPatternPassesShapeRequirement") to { config -> RepoPatternPassesShapeRequirement(config, repoContext) },
        ),
    )
}
