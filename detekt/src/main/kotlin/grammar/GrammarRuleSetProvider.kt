package grammar

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class GrammarRuleSetProvider : SkyHanniRuleSetProvider("GrammarRules") {
    init {
        rule(::AvoidBritishSpelling)
    }
}
