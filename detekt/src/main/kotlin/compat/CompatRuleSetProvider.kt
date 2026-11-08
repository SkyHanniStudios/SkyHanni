package compat

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class CompatRuleSetProvider : SkyHanniRuleSetProvider("CompatRules") {
    override fun rules(): Map<String, (Config) -> Rule> = mapOf(
        "MinecraftCompat" to ::MinecraftCompat,
        "VanillaItemStackImport" to ::VanillaItemStackImport,
    )
}
