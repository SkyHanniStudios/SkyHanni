package style

import SkyHanniRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtValueArgument
import utils.DetektUtils.getAnnotation

class OnlyOnIslandSpecificity(config: Config) : SkyHanniRule(
    config,
    "`onlyOnSkyblock = true` provides no value when `onlyOnIsland` is present, as it is implicitly true.",
) {

    override fun visitNamedFunction(function: KtNamedFunction) {
        function.getAnnotation("HandleEvent")?.let { annotation ->
            val arguments = annotation.valueArguments
            val onlyOnSkyblock = arguments.firstOrNull { it.asElement().text.contains("onlyOnSkyblock") }
            val hasOnlyOnIsland = arguments.any { it.asElement().text.contains("onlyOnIsland") }

            if (onlyOnSkyblock != null && hasOnlyOnIsland) {
                onlyOnSkyblock.asElement().reportIssue(
                    "This instance of `onlyOnSkyblock = true` should be removed. `onlyOnIsland` implicitly covers this check.",
                    annotation,
                )
                val isPlainFlag = onlyOnSkyblock.getArgumentName()?.asName?.asString() == "onlyOnSkyblock" &&
                    onlyOnSkyblock.getArgumentExpression()?.text == "true"
                if (isPlainFlag && arguments.all { it.getArgumentName() != null }) (onlyOnSkyblock as? KtValueArgument)?.deleteArgument()
            }
        }

        super.visitNamedFunction(function)
    }
}
