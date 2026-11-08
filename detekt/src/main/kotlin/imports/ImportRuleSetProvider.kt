package imports

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class ImportRuleSetProvider : SkyHanniRuleSetProvider("ImportRules") {
    override fun rules(): Map<String, (Config) -> Rule> = mapOf(
        "CustomImportOrdering" to ::CustomImportOrdering,
    )
}
