package events

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.eventParameterType
import utils.SkyHanniSymbols.explicitEventTypes
import utils.SkyHanniSymbols.hasAnnotation
import utils.SkyHanniSymbols.isObjectMember
import utils.SkyHanniSymbols.isReferencedInFile

class UnusedEventParameter(config: Config, private val index: PrimaryFunctionIndex) : SkyHanniAnalysisRule(
    config,
    "@HandleEvent functions should drop an unused event parameter when the event type is already known from the " +
        "@PrimaryFunction name or an explicit eventType.",
) {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (!function.isObjectMember()) return
        val name = function.name ?: return
        val parameter = function.valueParameters.singleOrNull() ?: return
        val source = analyze(function) {
            if (!function.symbol.hasAnnotation(SkyHanniFqNames.HANDLE_EVENT_ID)) return@analyze null
            val eventId = (eventParameterType(function) as? KaClassType)?.classId ?: return@analyze null
            if (isUsed(parameter, function) || isReferencedInFile(function)) return@analyze null
            val primary = index.lookup(this, function.project, name)
            when {
                primary.isNotEmpty() -> "@PrimaryFunction(\"$name\")".takeIf { primary == listOf(eventId) }
                function.symbol.explicitEventTypes() == listOf(eventId) -> "eventType"
                else -> null
            }
        } ?: return
        if (function.hasOverloadSibling(name)) return
        parameter.reportIssue("Parameter '${parameter.name}' is never used and the event type is already known from $source.", function)
        if (function.hasModifier(KtTokens.PRIVATE_KEYWORD)) function.valueParameterList?.replaceText("()")
    }

    @OptIn(KaExperimentalApi::class)
    private fun KaSession.isUsed(parameter: KtParameter, function: KtNamedFunction): Boolean =
        function.bodyExpression
            ?.collectDescendantsOfType<KtNameReferenceExpression> { it.getReferencedName() == parameter.name }
            .orEmpty()
            .any { it.resolveSymbol()?.psi == parameter }

    private fun KtNamedFunction.hasOverloadSibling(name: String): Boolean =
        containingClassOrObject?.declarations.orEmpty().any { it != this && it is KtNamedFunction && it.name == name }
}
