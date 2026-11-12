package style

import SkyHanniRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

class NoLorenzVec(config: Config) : SkyHanniRule(
    config,
    "LorenzVec is deprecated. Use SkyHanniVector or SkyHanniBlockVector from utils.vector.",
) {

    override fun visitKtFile(file: KtFile) {
        if (EXEMPT_FILES.any { file.virtualFilePath.endsWith(it) }) return
        val usages = file.collectDescendantsOfType<KtSimpleNameExpression> {
            it.getReferencedName() == LEGACY_NAME && it.getParentOfType<KtImportDirective>(strict = true) == null
        }
        val first = usages.firstOrNull() ?: return
        first.reportIssue("${usages.size} LorenzVec usages in this file. Use SkyHanniVector or SkyHanniBlockVector.")
    }

    companion object {
        private const val LEGACY_NAME = "LorenzVec"
        private val EXEMPT_FILES = listOf("utils/LorenzVec.kt", "utils/vector/SkyHanniVector.kt")
    }
}
