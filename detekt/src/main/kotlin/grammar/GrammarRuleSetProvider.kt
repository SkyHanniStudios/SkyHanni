package grammar

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class GrammarRuleSetProvider : SkyHanniRuleSetProvider("GrammarRules") {
    override fun rules(): Map<String, (Config) -> Rule> = mapOf(
        "AvoidBritishSpelling" to ::AvoidBritishSpelling,
    )
}
