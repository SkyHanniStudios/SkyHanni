package formatting

import PreprocessingPattern.Companion.containsPreprocessingPattern
import SkyHanniRule
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiWhiteSpace
import dev.detekt.api.Config
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.psiUtil.nextLeaf
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * This rule enforces the default spacing rules for annotations but allows preprocessed comments to be between
 * an annotation and the annotated construct.
 */
class CustomAnnotationSpacing(config: Config) : SkyHanniRule(
    config,
    "Annotations should directly precede the annotated construct, with only Stonecutter comments in between.",
) {

    override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry) {
        val nextNodes = annotationEntry.nextLeaf()?.siblings()?.takeWhile { it is PsiWhiteSpace || it is PsiComment }?.toList().orEmpty()

        val hasInvalidSpacing = nextNodes.any { nextNode ->
            when (nextNode) {
                is PsiWhiteSpace -> nextNode.isInvalid()
                is PsiComment -> nextNode.isInvalid()
                else -> false
            }
        } && !annotationEntry.text.contains("file:")

        if (hasInvalidSpacing) {
            annotationEntry.reportIssue("Annotations should occur immediately before the annotated construct.")
            nextNodes.filterIsInstance<PsiWhiteSpace>().filter { it.isInvalid() }.forEach {
                correct(annotationEntry, it.textRange.startOffset, it.textRange.endOffset, "\n" + it.text.substringAfterLast('\n'))
            }
        }
        super.visitAnnotationEntry(annotationEntry)
    }

    private fun PsiWhiteSpace.isInvalid(): Boolean {
        return text.indexOf('\n') != text.lastIndexOf('\n')
    }

    private fun PsiComment.isInvalid(): Boolean {
        return !text.containsPreprocessingPattern()
    }
}
