package style

import dev.detekt.api.Config
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import utils.DetektUtils.getAnnotation
import utils.SkyHanniFqNames

class InSkyBlockEarlyReturn(config: Config) : HandleEventEarlyReturnRule(
    config,
    "Event handlers should use onlyOnSkyblock = true on @HandleEvent instead of returning early on SkyBlockUtils.inSkyBlock.",
) {

    private fun KtExpression.containsInSkyBlockCheck(): Boolean = text.contains(IN_SKYBLOCK)

    private fun KtIfExpression.inSkyBlockCheck(): KtExpression? {
        val check = collectDescendantsOfType<KtDotQualifiedExpression> { it.text == IN_SKYBLOCK }.firstOrNull() ?: return null
        return (check.parent as? KtPrefixExpression)?.takeIf { it.negatedOperand() == IN_SKYBLOCK } ?: check
    }

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        val annotation = function.getAnnotation(SkyHanniFqNames.HANDLE_EVENT) ?: return
        val ifExpressions = function.bodyExpression?.collectDescendantsOfType<KtIfExpression>() ?: return

        for (ifExpression in ifExpressions) {
            if (!ifExpression.containsInSkyBlockCheck() || !ifExpression.isEarlyReturn()) continue
            (ifExpression.inSkyBlockCheck() ?: ifExpression).reportIssue(
                "This early return should be replaced with onlyOnSkyblock = true in @HandleEvent annotation",
                ifExpression,
            )
            correctGuard(function, annotation, ifExpression)
        }
    }

    private fun correctGuard(function: KtNamedFunction, annotation: KtAnnotationEntry, ifExpression: KtIfExpression) {
        val operand = guardCandidate(function, ifExpression) ?: return
        if (operand.negatedOperand() != IN_SKYBLOCK) return
        val names = annotation.argumentNames()
        if (OUTSIDE_SKYBLOCK_ARGUMENT in names || names.any { it in ISLAND_ARGUMENTS }) return
        val skyblockArgument = annotation.namedArgument(SKYBLOCK_ARGUMENT)
        if (skyblockArgument != null && !KtPsiUtil.isTrueConstant(skyblockArgument.getArgumentExpression())) return
        if (!removeGuard(ifExpression, operand)) return
        if (skyblockArgument == null) annotation.addArgument("$SKYBLOCK_ARGUMENT = true")
    }

    companion object {
        private const val IN_SKYBLOCK = "SkyBlockUtils.inSkyBlock"
    }
}
