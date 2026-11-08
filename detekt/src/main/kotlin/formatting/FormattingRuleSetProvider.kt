package formatting

import SkyHanniRuleSetProvider
import com.google.auto.service.AutoService
import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class FormattingRuleSetProvider : SkyHanniRuleSetProvider("FormattingRules") {
    override fun rules(): Map<String, (Config) -> Rule> = mapOf(
        "CustomAnnotationSpacing" to ::CustomAnnotationSpacing,
        "CustomCommentSpacing" to ::CustomCommentSpacing,
        "StorageVarOrVal" to ::StorageVarOrVal,
    )
}
