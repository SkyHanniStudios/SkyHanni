package events

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtNamedFunction
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.eventParameterType
import utils.SkyHanniSymbols.eventReceiverType
import utils.SkyHanniSymbols.explicitEventTypes
import utils.SkyHanniSymbols.isObjectMember

class RedundantHandleEvent(config: Config, private val index: PrimaryFunctionIndex) : SkyHanniAnalysisRule(
    config,
    "@HandleEvent should only be applied to functions that take a SkyHanniEvent, list an event type, or are named after a @PrimaryFunction.",
) {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (!function.isObjectMember()) return
        val name = function.name ?: return
        val annotation = analyze(function) {
            val symbol = function.symbol
            val entry = symbol.annotations[SkyHanniFqNames.HANDLE_EVENT_ID].firstOrNull()?.psi as? KtAnnotationEntry
                ?: return@analyze null
            val handlesEvent = eventParameterType(function) != null ||
                eventReceiverType(function) != null ||
                symbol.explicitEventTypes().isNotEmpty() ||
                index.lookup(this, function.project, name).isNotEmpty()
            entry.takeUnless { handlesEvent }
        } ?: return
        (function.nameIdentifier ?: function).reportIssue(
            "Function should not be annotated with @HandleEvent if it does not take a SkyHanniEvent.",
            function,
        )
        annotation.deleteText()
    }
}
