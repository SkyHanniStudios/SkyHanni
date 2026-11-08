package repo

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class RepoRuleSetProvider : SkyHanniRuleSetProvider("RepoRules") {
    override fun rules(): Map<String, (Config) -> Rule> {
        val context = RepoPatternContext()
        return mapOf(
            "SkullTexturesUseRepo" to ::SkullTexturesUseRepo,
            "RepoPatternRegexTestFailed" to { config -> RepoPatternRegexTestFailed(config, context) },
            "RepoPatternRegexTestMissing" to { config -> RepoPatternRegexTestMissing(config, context) },
            "RepoPatternUnnamedGroup" to { config -> RepoPatternUnnamedGroup(config, context) },
            "RepoPatternPassesShapeRequirement" to { config -> RepoPatternPassesShapeRequirement(config, context) },
        )
    }
}
