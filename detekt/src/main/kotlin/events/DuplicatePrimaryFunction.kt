package events

import SkyHanniAnalysisRule
import com.intellij.psi.PsiElement
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtClassOrObject
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.isSkyHanniEventClass
import utils.SkyHanniSymbols.primaryFunctionName

class DuplicatePrimaryFunction(config: Config, private val index: PrimaryFunctionIndex) : SkyHanniAnalysisRule(
    config,
    "Each @PrimaryFunction name must belong to exactly one event, because handlers are dispatched by name.",
) {

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        super.visitClassOrObject(classOrObject)
        if (classOrObject.name == null) return
        val finding = analyze(classOrObject) {
            val symbol = classOrObject.classSymbol ?: return@analyze null
            if (!isSkyHanniEventClass(symbol)) return@analyze null
            val name = symbol.primaryFunctionName() ?: return@analyze null
            val duplicates = index.lookup(this, classOrObject.project, name).filter { it != symbol.classId }
            if (duplicates.isEmpty()) return@analyze null
            val anchor: PsiElement? = symbol.annotations[SkyHanniFqNames.PRIMARY_FUNCTION_ID].firstOrNull()
                ?.psi?.valueArguments?.firstOrNull()?.getArgumentExpression()
            val message = "@PrimaryFunction(\"$name\") is also used by: ${duplicates.joinToString { it.shortClassName.asString() }}"
            (anchor ?: classOrObject.nameIdentifier ?: classOrObject) to message
        } ?: return
        finding.first.reportIssue(finding.second, classOrObject)
    }
}
