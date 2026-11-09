package events

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
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
import utils.SkyHanniSymbols.isReferencedInFile
import utils.SkyHanniSymbols.primaryFunctionName

class MismatchedPrimaryFunction(config: Config) : SkyHanniAnalysisRule(
    config,
    "@HandleEvent functions should be named after the @PrimaryFunction of the event they handle.",
) {

    private data class Mismatch(val expected: String, val eventName: String, val renamable: Boolean)

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (!function.isObjectMember()) return
        val name = function.name ?: return
        val mismatch = analyze(function) {
            val event = handledEvent(function) ?: return@analyze null
            val expected = event.primaryFunctionName()?.takeIf { it != name } ?: return@analyze null
            val siblings = function.containingClassOrObject?.declarations.orEmpty().filter { it != function }
            if (siblings.any { it.name == expected }) return@analyze null
            val sameEvent = siblings.filterIsInstance<KtNamedFunction>().any { handledEvent(it)?.classId == event.classId }
            if (sameEvent) return@analyze null
            val renamable = function.hasModifier(KtTokens.PRIVATE_KEYWORD) && !isReferencedInFile(function)
            Mismatch(expected, event.displayName(), renamable)
        } ?: return
        (function.nameIdentifier ?: function).reportIssue(
            "Handler name '$name' does not match @PrimaryFunction(\"${mismatch.expected}\") on ${mismatch.eventName}.",
            function,
        )
        if (mismatch.renamable) function.nameIdentifier?.replaceText(mismatch.expected)
    }

    private fun KaSession.handledEvent(function: KtNamedFunction): KaClassSymbol? {
        val symbol = function.symbol
        if (!symbol.hasAnnotation(SkyHanniFqNames.HANDLE_EVENT_ID)) return null
        return symbol.explicitEventTypes().firstOrNull()?.let { findClass(it) }
            ?: (eventParameterType(function) ?: eventReceiverType(function))?.expandedSymbol
    }
}
