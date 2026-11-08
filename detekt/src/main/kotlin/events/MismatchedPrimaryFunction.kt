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
import utils.SkyHanniSymbols.explicitEventTypes
import utils.SkyHanniSymbols.hasAnnotation
import utils.SkyHanniSymbols.isObjectMember
import utils.SkyHanniSymbols.primaryFunctionName

class MismatchedPrimaryFunction(config: Config) : SkyHanniAnalysisRule(
    config,
    "@HandleEvent functions should be named after the @PrimaryFunction of the event they handle.",
) {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (!function.isObjectMember()) return
        val name = function.name ?: return
        val (expected, eventName) = analyze(function) {
            val symbol = function.symbol
            if (!symbol.hasAnnotation(SkyHanniFqNames.HANDLE_EVENT_ID)) return@analyze null
            val event = symbol.explicitEventTypes().firstOrNull()?.let { findClass(it) }
                ?: (eventParameterType(function) ?: eventReceiverType(function))?.expandedSymbol
                ?: return@analyze null
            val expected = event.primaryFunctionName()?.takeIf { it != name } ?: return@analyze null
            expected to event.displayName()
        } ?: return
        if (function.siblingNames().contains(expected)) return
        val identifier = function.nameIdentifier ?: function
        identifier.reportIssue("Handler name '$name' does not match @PrimaryFunction(\"$expected\") on $eventName.", function)
        if (function.canRenameTo(expected)) function.nameIdentifier?.replaceText(expected)
    }

    private fun KtNamedFunction.siblingNames(): Set<String> =
        containingClassOrObject?.declarations.orEmpty().filter { it != this }.mapNotNull { it.name }.toSet()

    private fun KtNamedFunction.canRenameTo(name: String): Boolean =
        hasModifier(KtTokens.PRIVATE_KEYWORD) &&
            name !in siblingNames() &&
            containingFile.text.split(Regex("\\b${this.name}\\b")).size == 2
}
