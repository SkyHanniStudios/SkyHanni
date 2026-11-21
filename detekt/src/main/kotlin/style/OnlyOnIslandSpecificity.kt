package style

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiUtil
import utils.DetektUtils.getAnnotation
import utils.SkyHanniFqNames

class OnlyOnIslandSpecificity(config: Config) : SkyHanniAnalysisRule(
    config,
    "`onlyOnSkyblock = true` provides no value when `onlyOnIsland` is present, as it is implicitly true.",
) {

    override fun visitNamedFunction(function: KtNamedFunction) {
        function.getAnnotation(SkyHanniFqNames.HANDLE_EVENT)?.let { annotation ->
            val onlyOnSkyblock = annotation.namedArgument(HandleEventEarlyReturnRule.SKYBLOCK_ARGUMENT)
            val hasOnlyOnIsland = annotation.argumentNames().any { it in HandleEventEarlyReturnRule.ISLAND_ARGUMENTS }

            if (onlyOnSkyblock != null && hasOnlyOnIsland) {
                onlyOnSkyblock.reportIssue(
                    "This instance of `onlyOnSkyblock = true` should be removed. `onlyOnIsland` implicitly covers this check.",
                    annotation,
                )
                val isPlainFlag = KtPsiUtil.isTrueConstant(onlyOnSkyblock.getArgumentExpression())
                if (isPlainFlag && annotation.valueArguments.all { it.getArgumentName() != null }) onlyOnSkyblock.deleteArgument()
            }
        }

        super.visitNamedFunction(function)
    }
}
