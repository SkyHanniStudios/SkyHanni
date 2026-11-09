package events

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtClassOrObject
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.hasAnnotation
import utils.SkyHanniSymbols.isAbstractDeclaration
import utils.SkyHanniSymbols.isSkyHanniEventClass

class MissingPrimaryFunction(config: Config) : SkyHanniAnalysisRule(
    config,
    "Concrete SkyHanniEvent classes should declare a @PrimaryFunction, so handlers do not need an explicit eventType.",
) {

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        super.visitClassOrObject(classOrObject)
        if (classOrObject.name == null) return
        val missing = analyze(classOrObject) {
            val symbol = classOrObject.classSymbol ?: return@analyze false
            !symbol.isAbstractDeclaration() &&
                isSkyHanniEventClass(symbol) &&
                !symbol.hasAnnotation(SkyHanniFqNames.PRIMARY_FUNCTION_ID)
        }
        if (missing) {
            (classOrObject.nameIdentifier ?: classOrObject).reportIssue(
                "Event should either have a @PrimaryFunction, or be abstract.",
                classOrObject,
            )
        }
    }
}
