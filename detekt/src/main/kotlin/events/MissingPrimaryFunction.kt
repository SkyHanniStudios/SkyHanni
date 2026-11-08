package events

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtClassOrObject
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.hasAnnotation
import utils.SkyHanniSymbols.isAbstractDeclaration
import utils.SkyHanniSymbols.isSkyHanniEventClass
import utils.SkyHanniSymbols.primaryFunctionCandidate

class MissingPrimaryFunction(config: Config, private val index: PrimaryFunctionIndex) : SkyHanniAnalysisRule(
    config,
    "Concrete SkyHanniEvent classes should declare a @PrimaryFunction, so handlers do not need an explicit eventType.",
) {

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        super.visitClassOrObject(classOrObject)
        val name = classOrObject.name ?: return
        val candidate = primaryFunctionCandidate(name)
        val fixable = analyze(classOrObject) {
            val symbol = classOrObject.classSymbol ?: return@analyze null
            if (symbol.isAbstractDeclaration() || !isSkyHanniEventClass(symbol)) return@analyze null
            if (symbol.hasAnnotation(SkyHanniFqNames.PRIMARY_FUNCTION_ID)) return@analyze null
            index.lookup(this, classOrObject.project, candidate).isEmpty()
        } ?: return
        (classOrObject.nameIdentifier ?: classOrObject).reportIssue(
            "Event should either have a @PrimaryFunction, or be abstract.",
            classOrObject,
        )
        if (fixable) classOrObject.addAnnotation(SkyHanniFqNames.PRIMARY_FUNCTION, "\"$candidate\"")
    }
}
