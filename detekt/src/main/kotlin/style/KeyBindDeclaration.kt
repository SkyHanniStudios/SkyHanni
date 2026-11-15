package style

import SkyHanniRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

class KeyBindDeclaration(config: Config) : SkyHanniRule(
    config,
    "SkyHanniKeyBind declarations must be registered, private, and backed directly by a config Property<Int>.",
) {
    override fun visitProperty(property: KtProperty) {
        val call = property.initializer as? KtCallExpression
        val declaresKeyBind = call?.calleeExpression?.text == KEY_BIND_CLASS
        val annotated = property.annotationEntries.any { it.shortName?.asString() == ANNOTATION }

        when {
            annotated && !declaresKeyBind -> "@$ANNOTATION is only valid on a property initialized with $KEY_BIND_CLASS(...)."
            !declaresKeyBind -> null
            !annotated -> "$KEY_BIND_CLASS `${property.name}` needs @$ANNOTATION, otherwise it is never registered."
            property.isVar || !property.hasModifier(KtTokens.PRIVATE_KEYWORD) -> "$KEY_BIND_CLASS `${property.name}` should be a private val."
            !call.hasDirectOption() ->
                "$KEY_BIND_CLASS `${property.name}` must reference its config Property<Int> directly, wrapping a plain Int breaks syncing."

            else -> null
        }?.let { property.reportIssue(it) }

        super.visitProperty(property)
    }

    private fun KtCallExpression.hasDirectOption(): Boolean {
        val named = valueArguments.find { it.getArgumentName()?.asName?.asString() == OPTION_PARAMETER }
        return when (val expression = (named ?: valueArguments.firstOrNull())?.getArgumentExpression()) {
            is KtCallableReferenceExpression -> true
            is KtLambdaExpression -> expression.bodyExpression?.statements?.singleOrNull()?.let { statement ->
                (statement is KtNameReferenceExpression || statement is KtDotQualifiedExpression) &&
                    statement.collectDescendantsOfType<KtCallExpression>().isEmpty()
            } ?: false

            else -> false
        }
    }

    companion object {
        private const val KEY_BIND_CLASS = "SkyHanniKeyBind"
        private const val ANNOTATION = "RegisterKeyBind"
        private const val OPTION_PARAMETER = "option"
    }
}
