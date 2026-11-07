package style

import SkyHanniRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import utils.DetektUtils.hasAnnotation

class PrivateEventListener(config: Config) : SkyHanniRule(
    config,
    "Functions annotated with @HandleEvent should be private.",
    CONVENTIONS_URI,
) {
    override fun visitNamedFunction(function: KtNamedFunction) {
        if (function.hasAnnotation("HandleEvent") && !isEffectivelyPrivate(function)) {
            (function.nameIdentifier ?: function).reportIssue("Event listener functions should be private.", function)
            correct(function)
        }
        super.visitNamedFunction(function)
    }

    private fun correct(function: KtNamedFunction) {
        if (BLOCKING_MODIFIERS.any(function::hasModifier)) return
        val visibility = VISIBILITY_MODIFIERS.firstNotNullOfOrNull { function.modifierList?.getModifier(it) }
        if (visibility != null) {
            visibility.replaceText("private")
            return
        }
        val funKeyword = function.funKeyword ?: return
        val firstModifier = function.modifierList?.node?.getChildren(null)
            ?.firstOrNull { it.elementType is KtModifierKeywordToken }?.psi
        (firstModifier ?: funKeyword).insertTextBefore("private ")
    }

    private fun isEffectivelyPrivate(function: KtNamedFunction): Boolean {
        if (function.hasModifier(KtTokens.PRIVATE_KEYWORD)) return true

        // If the function is inside a private class, it is effectively private
        val containingClass = function.getParentOfType<KtClassOrObject>(true)
        return containingClass?.hasModifier(KtTokens.PRIVATE_KEYWORD) == true
    }

    companion object {
        private val VISIBILITY_MODIFIERS = listOf(KtTokens.PUBLIC_KEYWORD, KtTokens.INTERNAL_KEYWORD, KtTokens.PROTECTED_KEYWORD)
        private val BLOCKING_MODIFIERS = listOf(KtTokens.OVERRIDE_KEYWORD, KtTokens.ABSTRACT_KEYWORD, KtTokens.OPEN_KEYWORD)
    }
}
