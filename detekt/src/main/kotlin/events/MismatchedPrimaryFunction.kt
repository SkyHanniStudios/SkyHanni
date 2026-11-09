package events

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import utils.SkyHanniSymbols.displayName
import utils.SkyHanniSymbols.eventParameterType
import utils.SkyHanniSymbols.eventReceiverType
import utils.SkyHanniSymbols.explicitEventTypes
import utils.SkyHanniSymbols.handleEventEntry
import utils.SkyHanniSymbols.isObjectMember
import utils.SkyHanniSymbols.isReferencedInFile
import utils.SkyHanniSymbols.primaryFunctionName

class MismatchedPrimaryFunction(config: Config) : SkyHanniAnalysisRule(
    config,
    "@HandleEvent functions should be named after the @PrimaryFunction of the event they handle.",
) {

    private data class Mismatch(
        val expected: String,
        val eventName: String,
        val renamable: Boolean,
        val redundantEventType: KtValueArgument?,
    )

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (!function.isObjectMember()) return
        val name = function.name ?: return
        val mismatch = analyze(function) {
            val event = handledEvent(function) ?: return@analyze null
            val expected = event.primaryFunctionName()?.takeIf { it != name } ?: return@analyze null
            val siblings = function.containingClassOrObject?.declarations.orEmpty().filter { it != function }
            if (siblings.any { it.name == expected }) return@analyze null
            if (siblings.filterIsInstance<KtNamedFunction>().any { handledEvent(it)?.classId == event.classId }) return@analyze null
            val explicit = function.symbol.explicitEventTypes()
            val eventTypeArgument = function.symbol.handleEventEntry()?.eventTypeArgument()
                ?.takeIf { explicit == listOfNotNull(event.classId) }
            Mismatch(expected, event.displayName(), !isReferencedInFile(function), eventTypeArgument)
        } ?: return
        (function.nameIdentifier ?: function).reportIssue(
            "Handler name '$name' does not match @PrimaryFunction(\"${mismatch.expected}\") on ${mismatch.eventName}.",
            function,
        )
        if (!mismatch.renamable) return
        if (function.nameIdentifier?.replaceText(mismatch.expected) == true) mismatch.redundantEventType?.removeArgument()
    }

    private fun KtAnnotationEntry.eventTypeArgument(): KtValueArgument? =
        valueArguments.filterIsInstance<KtValueArgument>().firstOrNull { argument ->
            val argumentName = argument.getArgumentName()?.asName?.asString()
            argumentName == EVENT_TYPE || (argumentName == null && valueArguments.indexOf(argument) == 0)
        }

    private fun KaSession.handledEvent(function: KtNamedFunction): KaClassSymbol? {
        val symbol = function.symbol
        symbol.handleEventEntry() ?: return null
        return symbol.explicitEventTypes().firstOrNull()?.let { findClass(it) }
            ?: (eventParameterType(function) ?: eventReceiverType(function))?.expandedSymbol
    }

    companion object {
        private const val EVENT_TYPE = "eventType"
    }
}
