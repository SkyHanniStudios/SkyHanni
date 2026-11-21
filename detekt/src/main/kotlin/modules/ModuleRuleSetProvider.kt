package modules

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class ModuleRuleSetProvider : SkyHanniRuleSetProvider("ModuleRules") {
    init {
        rule(::MissingSkyHanniModule)
        rule(::SkyHanniModuleOnlyOnObjects)
    }
}
