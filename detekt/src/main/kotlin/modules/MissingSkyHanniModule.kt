package modules

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.hasAnnotation
import utils.SkyHanniSymbols.isInSkyHanniPackage

class MissingSkyHanniModule(config: Config) : SkyHanniAnalysisRule(
    config,
    "Objects with @HandleEvent functions must be annotated with @SkyHanniModule, otherwise their handlers are never registered.",
) {

    override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
        super.visitObjectDeclaration(declaration)
        if (declaration.isObjectLiteral() || !declaration.isInSkyHanniPackage()) return
        val body = declaration.body ?: return
        val missing = analyze(declaration) {
            val symbol = declaration.classSymbol ?: return@analyze false
            !symbol.hasAnnotation(SkyHanniFqNames.SKYHANNI_MODULE_ID) &&
                body.functions.any { it.symbol.hasAnnotation(SkyHanniFqNames.HANDLE_EVENT_ID) }
        }
        if (!missing) return
        (declaration.nameIdentifier ?: declaration.getObjectKeyword() ?: declaration).reportIssue(
            "Module should have a @SkyHanniModule annotation.",
            declaration,
        )
        declaration.addAnnotation(SkyHanniFqNames.SKYHANNI_MODULE)
    }
}
