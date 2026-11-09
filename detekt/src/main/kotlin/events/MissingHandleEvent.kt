package events

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.displayName
import utils.SkyHanniSymbols.eventParameterType
import utils.SkyHanniSymbols.eventReceiverType
import utils.SkyHanniSymbols.hasAnnotation
import utils.SkyHanniSymbols.isObjectMember
import utils.SkyHanniSymbols.isReferencedInFile

class MissingHandleEvent(config: Config, private val index: PrimaryFunctionIndex) : SkyHanniAnalysisRule(
    config,
    "Private functions in a @SkyHanniModule that are never called, and take a SkyHanniEvent or are named after a " +
        "@PrimaryFunction, are event handlers missing @HandleEvent.",
) {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (!function.isObjectMember() || !function.hasModifier(KtTokens.PRIVATE_KEYWORD)) return
        val name = function.name ?: return
        val message = analyze(function) {
            val symbol = function.symbol
            if (symbol.hasAnnotation(SkyHanniFqNames.HANDLE_EVENT_ID) || !symbol.returnType.isUnitType) return@analyze null
            val module = function.containingClassOrObject?.classSymbol ?: return@analyze null
            if (!module.hasAnnotation(SkyHanniFqNames.SKYHANNI_MODULE_ID)) return@analyze null
            val parameterless = function.valueParameters.isEmpty()
            val eventType = eventParameterType(function) ?: eventReceiverType(function)?.takeIf { parameterless }
            val text = when {
                eventType != null -> "Function seems to handle ${eventType.displayName()}. Add @HandleEvent if this is an event handler."
                parameterless && function.receiverTypeReference == null -> index.lookup(this, name)?.firstOrNull()?.let {
                    "Function name matches @PrimaryFunction of ${it.shortClassName}. " +
                        "Either add @HandleEvent if this is an event handler, or rename the function."
                }
                else -> null
            }
            text?.takeUnless { isReferencedInFile(function) }
        } ?: return
        (function.nameIdentifier ?: function).reportIssue(message, function)
    }
}
