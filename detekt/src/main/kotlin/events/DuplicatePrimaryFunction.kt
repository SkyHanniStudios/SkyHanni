package events

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.isSkyHanniEventClass
import utils.SkyHanniSymbols.primaryFunctionCandidate
import utils.SkyHanniSymbols.primaryFunctionName

class DuplicatePrimaryFunction(config: Config, private val index: PrimaryFunctionIndex) : SkyHanniAnalysisRule(
    config,
    "Each @PrimaryFunction name must belong to exactly one event, because handlers are dispatched by name.",
) {

    private data class Duplicate(val name: String, val others: String, val value: KtExpression?, val rename: String?)

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        super.visitClassOrObject(classOrObject)
        if (classOrObject.name == null) return
        val duplicate = analyze(classOrObject) {
            val symbol = classOrObject.classSymbol ?: return@analyze null
            if (!isSkyHanniEventClass(symbol)) return@analyze null
            val name = symbol.primaryFunctionName() ?: return@analyze null
            val others = index.lookup(this, name).orEmpty().filter { it != symbol.classId }
            if (others.isEmpty()) return@analyze null
            val value = symbol.annotations[SkyHanniFqNames.PRIMARY_FUNCTION_ID].firstOrNull()
                ?.psi?.valueArguments?.firstOrNull()?.getArgumentExpression()
            val rename = symbol.primaryFunctionCandidate()
                ?.takeIf { it != name && index.lookup(this, it)?.isEmpty() == true }
            Duplicate(name, others.joinToString { it.shortClassName.asString() }, value, rename)
        } ?: return
        (duplicate.value ?: classOrObject.nameIdentifier ?: classOrObject).reportIssue(
            "@PrimaryFunction(\"${duplicate.name}\") is also used by: ${duplicate.others}",
            classOrObject,
        )
        val literal = duplicate.value as? KtStringTemplateExpression ?: return
        if (literal.hasInterpolation()) return
        duplicate.rename?.let { literal.replaceText("\"$it\"") }
    }
}
