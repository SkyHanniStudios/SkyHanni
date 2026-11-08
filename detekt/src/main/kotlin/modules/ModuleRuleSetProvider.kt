package modules

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class ModuleRuleSetProvider : SkyHanniRuleSetProvider("ModuleRules") {
    override fun rules(): Map<String, (Config) -> Rule> = mapOf(
        "MissingSkyHanniModule" to ::MissingSkyHanniModule,
        "SkyHanniModuleOnlyOnObjects" to ::SkyHanniModuleOnlyOnObjects,
    )
}
