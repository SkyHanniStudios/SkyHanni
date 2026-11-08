package modules

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.singleFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.hasAnnotation
import utils.SkyHanniSymbols.isInSkyHanniPackage

class MissingSkyHanniModule(config: Config) : SkyHanniAnalysisRule(
    config,
    "Objects with @HandleEvent functions or repo patterns must be annotated with @SkyHanniModule, otherwise they are never loaded.",
) {

    override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
        super.visitObjectDeclaration(declaration)
        if (declaration.isObjectLiteral() || !declaration.isInSkyHanniPackage()) return
        val body = declaration.body ?: return
        val missing = analyze(declaration) {
            val symbol = declaration.classSymbol ?: return@analyze false
            if (symbol.hasAnnotation(SkyHanniFqNames.SKYHANNI_MODULE_ID)) return@analyze false
            body.functions.any { it.symbol.hasAnnotation(SkyHanniFqNames.HANDLE_EVENT_ID) } ||
                body.properties.any { isRepoPatternLike(it) }
        }
        if (!missing) return
        (declaration.nameIdentifier ?: declaration.getObjectKeyword() ?: declaration).reportIssue(
            "Module should have a @SkyHanniModule annotation.",
            declaration,
        )
        declaration.addAnnotation(SkyHanniFqNames.SKYHANNI_MODULE)
    }

    private fun KaSession.isRepoPatternLike(property: KtProperty): Boolean {
        if (REPO_PATTERN_TYPES.any { property.returnType.isSubtypeOf(it) }) return true
        val call = when (val delegate = property.delegateExpression) {
            is KtCallExpression -> delegate
            is KtQualifiedExpression -> delegate.selectorExpression as? KtCallExpression
            else -> null
        } ?: return false
        val callableId = call.resolveToCall()?.singleFunctionCallOrNull()?.symbol?.callableId ?: return false
        return callableId.callableName.asString() in FACTORY_NAMES && callableId.classId in FACTORY_OWNERS
    }

    companion object {
        private val REPO_PATTERN_TYPES = listOf(
            SkyHanniFqNames.REPO_PATTERN_ID,
            SkyHanniFqNames.REPO_PATTERN_GROUP_ID,
            SkyHanniFqNames.REPO_PATTERN_LIST_ID,
        )
        private val FACTORY_OWNERS = setOf(
            SkyHanniFqNames.REPO_PATTERN_ID.createNestedClassId(Name.identifier("Companion")),
            SkyHanniFqNames.REPO_PATTERN_GROUP_ID,
        )
        private val FACTORY_NAMES = setOf("pattern", "list", "group", "exclusiveGroup")
    }
}
