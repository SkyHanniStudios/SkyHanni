package style

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class SkyHanniStyleProvider : SkyHanniRuleSetProvider("SkyHanniStyle") {
    init {
        rule(::InSkyBlockEarlyReturn)
        rule(::IsInIslandEarlyReturn)
        rule(::OnlyOnIslandSpecificity)
    }
}
