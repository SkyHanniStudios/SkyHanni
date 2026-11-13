package imports

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class ImportRuleSetProvider : SkyHanniRuleSetProvider("ImportRules") {
    init {
        rule(::CustomImportOrdering)
    }
}
