import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import corrections.AnnotationCorrections
import corrections.CorrectionBuffer
import corrections.OffsetMapper
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Location
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.SourceLocation
import dev.detekt.api.TextLocation
import dev.detekt.api.modifiedText
import org.jetbrains.kotlin.KtPsiSourceFileLinesMapping
import org.jetbrains.kotlin.diagnostics.DiagnosticUtils.getLineAndColumnRangeInPsiFile
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCollectionLiteralExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.elementsInRange
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import utils.DetektUtils.javaClassId
import utils.DetektUtils.resolvedClassId
import java.net.URI
import kotlin.io.path.Path

abstract class SkyHanniRule(config: Config, description: String, url: URI? = null) :
    Rule(config, description, url),
    AnnotationCorrections {

    private val corrections = CorrectionBuffer()

    private var isCorrectionPass = false

    final override fun visit(root: KtFile) {
        corrections.clear()
        super.visit(root)
        if (corrections.isEmpty) return
        val pending = root.modifiedText ?: root.text
        if (pending != root.text) rebase(root, pending)
        var text = pending
        var remainingPasses = MAX_CORRECTION_PASSES
        while (remainingPasses-- > 0) {
            val result = corrections.applyTo(text)
            text = result.text
            corrections.clear()
            if (result.skipped == 0 || this is RequiresAnalysisApi) break
            collectCorrections(root, text)
            if (corrections.isEmpty) break
        }
        corrections.clear()
        if (text != pending) root.modifiedText = text
    }

    private fun rebase(root: KtFile, pending: String) {
        if (this is RequiresAnalysisApi) return corrections.rebase(OffsetMapper(root.text, pending))
        corrections.clear()
        collectCorrections(root, pending)
    }

    private fun collectCorrections(root: KtFile, text: String) {
        isCorrectionPass = true
        try {
            super.visit(KtPsiFactory(root.project).createPhysicalFile(root.name, text))
        } finally {
            isCorrectionPass = false
        }
    }

    protected fun PsiElement.reportIssue(message: String, signature: PsiElement = this) {
        if (!isCorrectionPass) report(Finding(Entity.from(this, signature), message))
    }

    protected fun PsiElement.reportRange(range: TextRange, message: String, signature: PsiElement = this) {
        if (isCorrectionPass) return
        val file = containingFile as KtFile
        val lineAndColumnRange = getLineAndColumnRangeInPsiFile(file, range)
        val location = Location(
            source = SourceLocation(lineAndColumnRange.start.line, lineAndColumnRange.start.column),
            endSource = SourceLocation(lineAndColumnRange.end.line, lineAndColumnRange.end.column),
            text = TextLocation(range.startOffset, range.endOffset),
            path = Path(file.virtualFilePath),
        )
        val ktElement = getNonStrictParentOfType<KtElement>() ?: file
        report(Finding(Entity(Entity.from(signature).signature, location, ktElement), message))
    }

    protected fun reportIssue(
        message: String,
        lineIndex: Int,
        line: String,
        file: KtFile,
        sourceFileLinesMapping: KtPsiSourceFileLinesMapping? = null,
    ) {
        val robustSourceFileLinesMapping = sourceFileLinesMapping ?: KtPsiSourceFileLinesMapping(file)
        val offset = robustSourceFileLinesMapping.getLineStartOffset(lineIndex)
        val element = findFirstMeaningfulKtElementInParents(file, offset, line) ?: file
        element.reportRange(TextRange(offset, offset + line.length), message)
    }

    final override fun canCorrect(anchor: PsiElement): Boolean = autoCorrect && !anchor.isSuppressed()

    final override fun correct(anchor: PsiElement, start: Int, end: Int, replacement: String): Boolean =
        canCorrect(anchor) && corrections.add(start, end, replacement)

    private fun PsiElement.isSuppressed(): Boolean {
        val ids = setOf(ruleName.value, SUPPRESS_ALL)
        return parentsWithSelf.filterIsInstance<KtAnnotated>().flatMap { it.annotationEntries }.any { entry ->
            entry.isSuppressAnnotation() && entry.suppressedIds().any { it.substringAfterLast(':').removePrefix(DETEKT_PREFIX) in ids }
        }
    }

    private fun KtAnnotationEntry.isSuppressAnnotation(): Boolean =
        if (this@SkyHanniRule is RequiresAnalysisApi && !isCorrectionPass) resolvedClassId() in SUPPRESS_ANNOTATIONS
        else SUPPRESS_ANNOTATIONS.any { it.shortClassName == shortName }

    private fun KtAnnotationEntry.suppressedIds(): List<String> =
        valueArguments.mapNotNull { it.getArgumentExpression() }
            .flatMap { (it as? KtCollectionLiteralExpression)?.innerExpressions ?: listOf(it) }
            .filterIsInstance<KtStringTemplateExpression>()
            .filterNot { it.hasInterpolation() }
            .map { template -> template.entries.joinToString("") { it.text } }

    companion object {
        val CONVENTIONS_URI = URI("https://github.com/SkyHanniStudios/SkyHanni/blob/beta/CONTRIBUTING.md#coding-styles-and-conventions")

        private const val MAX_CORRECTION_PASSES = 5
        private const val SUPPRESS_ALL = "all"
        private const val DETEKT_PREFIX = "detekt."
        private val SUPPRESS_ANNOTATIONS = setOf(Suppress::class.javaClassId(), SuppressWarnings::class.javaClassId())
        private val BLANK_OR_QUOTES = """[\s"]*""".toRegex()

        private fun findFirstMeaningfulKtElementInParents(
            file: KtFile,
            offset: Int,
            line: String,
        ): PsiElement? =
            findKtElementInParents(file, offset, line)
                .firstOrNull { !BLANK_OR_QUOTES.matches(it.text) }

        private fun findKtElementInParents(file: KtFile, offset: Int, line: String): Sequence<PsiElement> =
            file.elementsInRange(TextRange.create(offset, offset + line.length))
                .asSequence()
                .plus(file.findElementAt(offset))
                .mapNotNull { it?.getNonStrictParentOfType() }

    }
}
