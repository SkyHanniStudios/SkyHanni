package style

import dev.detekt.api.Config
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import utils.DetektUtils.getAnnotation
import utils.SkyHanniFqNames

class IsInIslandEarlyReturn(config: Config) : HandleEventEarlyReturnRule(
    config,
    "Event handlers should use onlyOnIsland on @HandleEvent instead of returning early on a negated island check.",
) {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        val annotation = function.getAnnotation(SkyHanniFqNames.HANDLE_EVENT) ?: return
        val ifExpressions = function.bodyExpression?.collectDescendantsOfType<KtIfExpression>() ?: return

        for (ifExpression in ifExpressions) {
            if (!ifExpression.isEarlyReturn()) continue
            val issueSpecific = regexMap.entries.firstOrNull { it.key.matches(ifExpression.text) }?.value ?: continue
            val check = ifExpression.collectDescendantsOfType<KtPrefixExpression> { it.islandFor() != null }.firstOrNull()
            (check ?: ifExpression).reportIssue(
                "This early return should be replaced with onlyOnIsland = $issueSpecific in the @HandleEvent annotation",
                ifExpression,
            )
            correctGuard(function, annotation, ifExpression)
        }
    }

    private fun KtExpression.islandFor(): String? {
        val operand = negatedOperand() ?: return null
        correctableChecks[operand]?.let { return it }
        return islandTypeCheck.matchEntire(operand)?.let { "IslandType.${it.groupValues[1]}" }
    }

    private fun correctGuard(function: KtNamedFunction, annotation: KtAnnotationEntry, ifExpression: KtIfExpression) {
        val operand = guardCandidate(function, ifExpression) ?: return
        val island = operand.islandFor() ?: return
        val names = annotation.argumentNames()
        if (names.any { it in ISLAND_ARGUMENTS } || OUTSIDE_SKYBLOCK_ARGUMENT in names) return
        if (!removeGuard(ifExpression, operand)) return
        val skyblockArgument = annotation.namedArgument(SKYBLOCK_ARGUMENT)
        if (skyblockArgument != null) {
            skyblockArgument.getArgumentExpression()?.replaceText(island)
            skyblockArgument.getArgumentName()?.replaceText(ISLAND_ARGUMENT)
        } else {
            annotation.addArgument("$ISLAND_ARGUMENT = $island")
        }
        function.containingKtFile.addImport(SkyHanniFqNames.ISLAND_TYPE)
    }

    companion object {
        private val isInIslandRegex = Regex(".*!IslandType\\..*\\.isInIsland\\(\\).*")
        private val isInGardenRegex = Regex(".*!GardenApi.inGarden\\(\\).*")
        private val isInRiftRegex = Regex(".*!RiftApi.inRift\\(\\).*")
        private val isInDungeonRegex = Regex(".*!DungeonApi.inDungeon\\(\\).*")
        private val isInKuudraRegex = Regex(".*!KuudraApi.inKuudra\\(\\).*")

        private val regexMap = mapOf(
            isInIslandRegex to "IslandType",
            isInGardenRegex to "IslandType.GARDEN",
            isInRiftRegex to "IslandType.THE_RIFT",
            isInDungeonRegex to "IslandType.CATACOMBS",
            isInKuudraRegex to "IslandType.KUUDRA_ARENA",
        )

        private val islandTypeCheck = Regex("IslandType\\.([A-Z0-9_]+)\\.isInIsland\\(\\)")

        private val correctableChecks = mapOf(
            "GardenApi.inGarden()" to "IslandType.GARDEN",
            "RiftApi.inRift()" to "IslandType.THE_RIFT",
            "DungeonApi.inDungeon()" to "IslandType.CATACOMBS",
        )
    }
}
