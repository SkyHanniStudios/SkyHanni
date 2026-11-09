package events

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtNamedFunction
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.eventParameterType
import utils.SkyHanniSymbols.eventReceiverType
import utils.SkyHanniSymbols.explicitEventTypes
import utils.SkyHanniSymbols.hasAnnotation
import utils.SkyHanniSymbols.isObjectMember

class RedundantHandleEvent(config: Config, private val index: PrimaryFunctionIndex) : SkyHanniAnalysisRule(
    config,
    "@HandleEvent should only be applied to functions that take a SkyHanniEvent, list an event type, or are named after a @PrimaryFunction.",
) {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (!function.isObjectMember()) return
        val name = function.name ?: return
        val redundant = analyze(function) {
            val symbol = function.symbol
            symbol.hasAnnotation(SkyHanniFqNames.HANDLE_EVENT_ID) &&
                eventParameterType(function) == null &&
                eventReceiverType(function) == null &&
                symbol.explicitEventTypes().isEmpty() &&
                index.lookup(this, name)?.isEmpty() == true
        }
        if (redundant) {
            (function.nameIdentifier ?: function).reportIssue(
                "Function should not be annotated with @HandleEvent if it does not take a SkyHanniEvent.",
                function,
            )
        }
    }
}
