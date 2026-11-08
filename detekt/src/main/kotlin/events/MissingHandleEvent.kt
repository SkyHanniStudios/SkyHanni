package events

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolVisibility
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.displayName
import utils.SkyHanniSymbols.eventParameterType
import utils.SkyHanniSymbols.eventReceiverType
import utils.SkyHanniSymbols.hasAnnotation
import utils.SkyHanniSymbols.isObjectMember

class MissingHandleEvent(config: Config, private val index: PrimaryFunctionIndex) : SkyHanniAnalysisRule(
    config,
    "Functions in objects that take a SkyHanniEvent, or are named after a @PrimaryFunction, should be annotated with @HandleEvent.",
) {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (!function.isObjectMember()) return
        if (function.receiverTypeReference != null && function.valueParameters.isNotEmpty()) return
        val name = function.name ?: return
        val message = analyze(function) {
            val symbol = function.symbol
            if (symbol.hasAnnotation(SkyHanniFqNames.HANDLE_EVENT_ID)) return@analyze null
            if (function.hasModifier(KtTokens.OPEN_KEYWORD)) return@analyze null
            if (function.hasModifier(KtTokens.OVERRIDE_KEYWORD) && symbol.visibility != KaSymbolVisibility.PUBLIC) return@analyze null
            val receiverOnly = function.valueParameters.isEmpty() && !function.hasModifier(KtTokens.PRIVATE_KEYWORD)
            val eventType = eventParameterType(function) ?: eventReceiverType(function)?.takeIf { receiverOnly }
            if (eventType != null) return@analyze "Function seems to handle ${eventType.displayName()}. Add @HandleEvent if this is an event handler."
            index.lookup(this, function.project, name).firstOrNull()?.let {
                "Function name matches @PrimaryFunction of ${it.shortClassName}. " +
                    "Either add @HandleEvent if this is an event handler, or rename the function."
            }
        } ?: return
        (function.nameIdentifier ?: function).reportIssue(message, function)
    }
}
