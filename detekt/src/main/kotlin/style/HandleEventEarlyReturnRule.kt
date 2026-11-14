package style

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtReturnExpression

abstract class HandleEventEarlyReturnRule(config: Config, description: String) : SkyHanniAnalysisRule(config, description) {

    protected fun KtExpression.isEarlyReturn(): Boolean = this is KtIfExpression && then is KtReturnExpression

    protected fun KtExpression.negatedOperand(): String? {
        val prefix = this as? KtPrefixExpression ?: return null
        if (prefix.operationToken != KtTokens.EXCL) return null
        return prefix.baseExpression?.text?.filterNot(Char::isWhitespace)
    }

    protected fun guardCandidate(function: KtNamedFunction, ifExpression: KtIfExpression): KtExpression? {
        if (!canCorrect(ifExpression)) return null
        val body = function.bodyBlockExpression ?: return null
        if (body.statements.firstOrNull() != ifExpression || ifExpression.`else` != null) return null
        val returnExpression = ifExpression.then as? KtReturnExpression ?: return null
        if (returnExpression.returnedExpression != null || returnExpression.getTargetLabel() != null) return null
        var operand = ifExpression.condition ?: return null
        while (operand is KtBinaryExpression && operand.operationToken == KtTokens.OROR) {
            operand = operand.left ?: return null
        }
        return operand
    }

    protected fun removeGuard(ifExpression: KtIfExpression, operand: KtExpression): Boolean {
        if (operand == ifExpression.condition) return ifExpression.deleteText(withFollowingBlankLine = true)
        val rest = (operand.parent as? KtBinaryExpression)?.right ?: return false
        return correct(ifExpression, operand.textRange.startOffset, rest.textRange.startOffset, "")
    }

    companion object {
        val ISLAND_ARGUMENTS = setOf("onlyOnIsland", "onlyOnIslands", "onlyOnIslandTypeTag")
        const val OUTSIDE_SKYBLOCK_ARGUMENT = "onlyOnSkyblockOrFeatures"
        const val SKYBLOCK_ARGUMENT = "onlyOnSkyblock"
        const val ISLAND_ARGUMENT = "onlyOnIsland"
    }
}
