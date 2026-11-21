package events

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtNamedFunction
import utils.SkyHanniSymbols.eventTypeArguments
import utils.SkyHanniSymbols.explicitEventTypes
import utils.SkyHanniSymbols.handleEventEntry
import utils.SkyHanniSymbols.isObjectMember

class RedundantEventType(config: Config, private val index: PrimaryFunctionIndex) : SkyHanniAnalysisRule(
    config,
    "@HandleEvent should not list an eventType that the function name already resolves to through @PrimaryFunction.",
) {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (!function.isObjectMember() || function.valueParameters.isNotEmpty() || function.receiverTypeReference != null) return
        val name = function.name ?: return
        val (argument, primary) = analyze(function) {
            val symbol = function.symbol
            val argument = symbol.handleEventEntry()?.eventTypeArguments()?.singleOrNull() ?: return@analyze null
            val primary = index.lookup(this, name)?.singleOrNull() ?: return@analyze null
            (argument to primary).takeIf { symbol.explicitEventTypes() == listOf(primary) }
        } ?: return
        argument.reportIssue(
            "This event type is redundant, '$name' already resolves to ${primary.shortClassName} through @PrimaryFunction.",
            function,
        )
        if (argument.removeArgument()) function.containingKtFile.removeImportIfUnused(primary, argument)
    }
}
