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
    "LorenzVec is deprecated. Use KVector from utils.vector.",
) {

    override fun visitKtFile(file: KtFile) {
        if (file.virtualFilePath.endsWith(EXEMPT_FILE)) return
        val usages = file.collectDescendantsOfType<KtSimpleNameExpression> {
            it.getReferencedName() == LEGACY_NAME && it.getParentOfType<KtImportDirective>(strict = true) == null
        }
        val first = usages.firstOrNull() ?: return
        first.reportIssue("${usages.size} LorenzVec usages in this file. Use KVector.")
    }

    companion object {
        private const val LEGACY_NAME = "LorenzVec"
        private const val EXEMPT_FILE = "utils/LorenzVec.kt"
    }
}
