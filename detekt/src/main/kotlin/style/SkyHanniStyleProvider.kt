package style

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class SkyHanniStyleProvider : SkyHanniRuleSetProvider("SkyHanniStyle") {
    override fun rules(): Map<String, (Config) -> Rule> = mapOf(
        "InSkyBlockEarlyReturn" to ::InSkyBlockEarlyReturn,
        "IsInIslandEarlyReturn" to ::IsInIslandEarlyReturn,
        "OnlyOnIslandSpecificity" to ::OnlyOnIslandSpecificity,
    )
}
