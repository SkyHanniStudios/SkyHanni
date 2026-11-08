package potentialbugs

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class PotentialBugsProvider : SkyHanniRuleSetProvider("potential-bugs") {
    override fun rules(): Map<String, (Config) -> Rule> = mapOf(
        "ImmutableTypesWithExpectedInteriorMutabilityInConfig" to ::ImmutableTypesWithExpectedInteriorMutabilityInConfig,
        "StorageNeedsExpose" to ::StorageNeedsExpose,
        "NonStorageDoesntNeedExpose" to ::NonStorageDoesntNeedExpose,
    )
}
