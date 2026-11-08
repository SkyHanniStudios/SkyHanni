import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.RuleName
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

abstract class SkyHanniRuleSetProvider(id: String) : RuleSetProvider {
    final override val ruleSetId: RuleSetId = RuleSetId(id)

    protected abstract fun rules(): Map<String, (Config) -> Rule>

    final override fun instance(): RuleSet = RuleSet(ruleSetId, rules().mapKeys { RuleName(it.key) })
}
