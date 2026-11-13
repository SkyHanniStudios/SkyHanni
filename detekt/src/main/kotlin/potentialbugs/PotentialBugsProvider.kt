package potentialbugs

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class PotentialBugsProvider : SkyHanniRuleSetProvider("potential-bugs") {
    init {
        rule(::ImmutableWritableConfigDeclaration)
        rule(::StorageNeedsExpose)
        rule(::NonStorageDoesntNeedExpose)
    }
}
