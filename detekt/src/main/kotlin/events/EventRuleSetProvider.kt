package events

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class EventRuleSetProvider : SkyHanniRuleSetProvider("EventRules") {
    private val index = PrimaryFunctionIndex()

    init {
        rule(::PrivateEventListener)
        rule { MissingHandleEvent(it, index) }
        rule { RedundantHandleEvent(it, index) }
        rule { MissingPrimaryFunction(it, index) }
        rule { DuplicatePrimaryFunction(it, index) }
        rule(::MismatchedPrimaryFunction)
        rule { UnusedEventParameter(it, index) }
        rule { RedundantEventType(it, index) }
    }
}
