package events

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.hasAnnotation
import utils.SkyHanniSymbols.isAbstractDeclaration
import utils.SkyHanniSymbols.isSkyHanniEventClass
import utils.SkyHanniSymbols.primaryFunctionCandidate

class MissingPrimaryFunction(config: Config, private val index: PrimaryFunctionIndex) : SkyHanniAnalysisRule(
    config,
    "Concrete SkyHanniEvent classes should declare a @PrimaryFunction, so handlers do not need an explicit eventType.",
) {

    private data class Missing(val candidate: String?)

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        super.visitClassOrObject(classOrObject)
        if (classOrObject.name == null) return
        val missing = analyze(classOrObject) {
            val symbol = classOrObject.classSymbol?.takeIf { needsPrimaryFunction(it) } ?: return@analyze null
            Missing(freeCandidate(classOrObject, symbol))
        } ?: return
        (classOrObject.nameIdentifier ?: classOrObject).reportIssue(
            "Event should either have a @PrimaryFunction, or be abstract.",
            classOrObject,
        )
        missing.candidate?.let { classOrObject.addAnnotation(SkyHanniFqNames.PRIMARY_FUNCTION, "\"$it\"") }
    }

    private fun KaSession.needsPrimaryFunction(symbol: KaClassSymbol): Boolean =
        !symbol.isAbstractDeclaration() && isSkyHanniEventClass(symbol) && !symbol.hasAnnotation(SkyHanniFqNames.PRIMARY_FUNCTION)

    private fun KaSession.freeCandidate(classOrObject: KtClassOrObject, symbol: KaClassSymbol): String? {
        val candidate = symbol.primaryFunctionCandidate() ?: return null
        if (index.lookup(this, candidate)?.isEmpty() != true) return null
        val sameFile = classOrObject.containingKtFile.collectDescendantsOfType<KtClassOrObject> { it != classOrObject }
            .mapNotNull { it.classSymbol?.takeIf { other -> needsPrimaryFunction(other) } }
        return candidate.takeIf { primaryFuncCandidate ->
            sameFile.none { it.primaryFunctionCandidate() == primaryFuncCandidate }
        }
    }
}
