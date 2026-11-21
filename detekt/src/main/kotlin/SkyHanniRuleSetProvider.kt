import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.RuleName
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

abstract class SkyHanniRuleSetProvider(id: String) : RuleSetProvider {

    final override val ruleSetId: RuleSetId = RuleSetId(id)

    private val rules = mutableMapOf<RuleName, (Config) -> Rule>()

    protected inline fun <reified T : Rule> rule(noinline factory: (Config) -> T) = register(T::class.java.simpleName, factory)

    protected fun register(name: String, factory: (Config) -> Rule) {
        rules[RuleName(name)] = factory
    }

    final override fun instance(): RuleSet = RuleSet(ruleSetId, rules.toMap())
}
