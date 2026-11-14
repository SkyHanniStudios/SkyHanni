package compat

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class CompatRuleSetProvider : SkyHanniRuleSetProvider("CompatRules") {
    init {
        rule(::MinecraftCompat)
        rule(::VanillaItemStackImport)
    }
}
