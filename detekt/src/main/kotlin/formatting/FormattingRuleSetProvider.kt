package formatting

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class FormattingRuleSetProvider : SkyHanniRuleSetProvider("FormattingRules") {
    init {
        rule(::CustomAnnotationSpacing)
        rule(::CustomCommentSpacing)
        rule(::StorageVarOrVal)
    }
}
