package events

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class EventRuleSetProvider : SkyHanniRuleSetProvider("EventRules") {
    override fun rules(): Map<String, (Config) -> Rule> {
        val index = PrimaryFunctionIndex()
        return mapOf(
            "PrivateEventListener" to ::PrivateEventListener,
            "MissingHandleEvent" to { config -> MissingHandleEvent(config, index) },
            "RedundantHandleEvent" to { config -> RedundantHandleEvent(config, index) },
            "MissingPrimaryFunction" to ::MissingPrimaryFunction,
            "DuplicatePrimaryFunction" to { config -> DuplicatePrimaryFunction(config, index) },
            "MismatchedPrimaryFunction" to ::MismatchedPrimaryFunction,
            "UnusedEventParameter" to { config -> UnusedEventParameter(config, index) },
        )
    }
}
