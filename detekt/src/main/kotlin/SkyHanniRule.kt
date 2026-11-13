import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Location
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.SourceLocation
import dev.detekt.api.TextLocation
import dev.detekt.api.modifiedText
import imports.ImportOrdering
import org.jetbrains.kotlin.KtPsiSourceFileLinesMapping
import org.jetbrains.kotlin.diagnostics.DiagnosticUtils.getLineAndColumnRangeInPsiFile
import org.jetbrains.kotlin.kdoc.psi.impl.KDocName
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.elementsInRange
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.psi.psiUtil.siblings
import java.net.URI
import kotlin.io.path.Path

abstract class SkyHanniRule(config: Config, description: String, url: URI? = null) : Rule(config, description, url) {

    private data class Correction(val start: Int, val end: Int, val replacement: String)

    private val corrections = mutableListOf<Correction>()

    protected var isCorrectionPass = false
        private set

    final override fun visit(root: KtFile) {
        corrections.clear()
        super.visit(root)
        if (corrections.isEmpty()) return
        val pending = root.modifiedText ?: root.text
        if (pending != root.text) {
            if (this is RequiresAnalysisApi) {
                corrections.clear()
                return
            }
            corrections.clear()
            isCorrectionPass = true
            try {
                super.visit(KtPsiFactory(root.project).createPhysicalFile(root.name, pending))
            } finally {
                isCorrectionPass = false
            }
        }
        val corrected = applyCorrections(pending)
        corrections.clear()
        if (corrected != pending) root.modifiedText = corrected
    }

    private fun applyCorrections(text: String): String {
        val builder = StringBuilder(text)
        var limit = text.length
        corrections.distinct()
            .sortedWith(compareByDescending<Correction> { it.start }.thenByDescending { it.end })
            .forEach { correction ->
                if (correction.end > limit) return@forEach
                builder.replace(correction.start, correction.end, correction.replacement)
                limit = correction.start
            }
        return builder.toString()
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

    protected fun canCorrect(anchor: PsiElement): Boolean = autoCorrect && !anchor.isSuppressed()

    protected fun correct(anchor: PsiElement, start: Int, end: Int, replacement: String): Boolean {
        if (!canCorrect(anchor)) return false
        corrections += Correction(start, end, replacement)
        return true
    }

    protected fun KtValueArgument.deleteArgument(): Boolean {
        val arguments = (parent as? KtValueArgumentList)?.arguments ?: return false
        val index = arguments.indexOf(this)
        return when {
            index > 0 -> correct(this, arguments[index - 1].textRange.endOffset, textRange.endOffset, "")
            arguments.size > 1 -> correct(this, textRange.startOffset, arguments[1].textRange.startOffset, "")
            else -> correct(this, textRange.startOffset, textRange.endOffset, "")
        }
    }

    protected fun KtAnnotationEntry.addArgument(argument: String): Boolean {
        val list = valueArgumentList
        val last = list?.arguments?.lastOrNull()
        if (last != null) {
            val text = containingFile.text
            val lineStart = text.lastIndexOf('\n', last.textRange.startOffset - 1) + 1
            val indent = text.substring(lineStart, last.textRange.startOffset)
            val separator = if (list.textContains('\n') && indent.isBlank()) ",\n$indent" else ", "
            return correct(this, last.textRange.endOffset, last.textRange.endOffset, "$separator$argument")
        }
        if (list != null) return correct(this, list.textRange.startOffset + 1, list.textRange.endOffset - 1, argument)
        val end = calleeExpression?.textRange?.endOffset ?: return false
        return correct(this, end, end, "($argument)")
    }

    protected fun KtAnnotationEntry.argumentNames(): List<String> =
        valueArguments.mapNotNull { it.getArgumentName()?.asName?.asString() }

    protected fun KtAnnotationEntry.namedArgument(name: String): KtValueArgument? =
        valueArgumentList?.arguments?.firstOrNull { it.getArgumentName()?.asName?.asString() == name }

    protected fun PsiElement.replaceText(replacement: String): Boolean =
        correct(this, textRange.startOffset, textRange.endOffset, replacement)

    protected fun PsiElement.insertTextBefore(text: String): Boolean =
        correct(this, textRange.startOffset, textRange.startOffset, text)

    protected fun PsiElement.ownLines(): TextRange? {
        val text = containingFile.text
        val start = text.lastIndexOf('\n', textRange.startOffset - 1) + 1
        val end = text.indexOf('\n', textRange.endOffset).let { if (it == -1) text.length else it + 1 }
        val alone = text.substring(start, textRange.startOffset).isBlank() && text.substring(textRange.endOffset, end).isBlank()
        return TextRange(start, end).takeIf { alone }
    }

    protected fun PsiElement.deleteText(withFollowingBlankLine: Boolean = false): Boolean {
        val text = containingFile.text
        ownLines()?.let { lines ->
            val next = text.indexOf('\n', lines.endOffset)
            val blankFollows = withFollowingBlankLine && next != -1 && text.substring(lines.endOffset, next).isBlank()
            return correct(this, lines.startOffset, if (blankFollows) next + 1 else lines.endOffset, "")
        }
        var end = textRange.endOffset
        while (end < text.length && (text[end] == ' ' || text[end] == '\t')) end++
        return correct(this, textRange.startOffset, end, "")
    }

    protected fun KtDeclaration.addAnnotation(fqName: String, arguments: String? = null): Boolean {
        val anchor = modifierList?.takeIf { it.textLength > 0 }
            ?: firstChild?.siblings()?.firstOrNull { it !is PsiWhiteSpace && it !is PsiComment }
            ?: return false
        if (!canCorrect(anchor)) return false
        val text = containingFile.text
        val lineStart = text.lastIndexOf('\n', anchor.textRange.startOffset - 1) + 1
        val indent = text.substring(lineStart, anchor.textRange.startOffset)
        val separator = if (indent.isBlank()) "\n$indent" else " "
        val argumentList = arguments?.let { "($it)" }.orEmpty()
        anchor.insertTextBefore("@${fqName.substringAfterLast('.')}$argumentList$separator")
        containingKtFile.addImport(fqName)
        return true
    }

    protected fun KtAnnotationEntry.removeAnnotation(fqName: String): Boolean {
        val shortName = fqName.substringAfterLast('.')
        val file = containingKtFile
        val others = file.collectDescendantsOfType<KtAnnotationEntry> { it != this && it.shortName?.asString() == shortName }
        if (!deleteText()) return false
        if (others.isEmpty()) file.removeImport(fqName)
        return true
    }

    protected fun KtValueArgument.removeArgument(): Boolean {
        val list = parent as? KtValueArgumentList ?: return false
        if (list.arguments.any { it != this && it.getArgumentName() == null }) return false
        return if (list.arguments.size == 1) list.replaceText("") else deleteArgument()
    }

    protected fun KtFile.removeImport(fqName: String): Boolean {
        val directive = importDirectives.firstOrNull { it.importedFqName?.asString() == fqName && it.aliasName == null } ?: return false
        return directive.deleteText()
    }

    protected fun KtFile.removeImportIfUnused(classId: ClassId, removed: PsiElement) {
        listOf(classId, classId.outermostClassId).distinct().forEach { id ->
            val shortName = id.shortClassName.asString()
            val inCode = collectDescendantsOfType<KtNameReferenceExpression> {
                it.getReferencedName() == shortName && it.getStrictParentOfType<KtImportDirective>() == null && !removed.isAncestor(it)
            }
            val inDocs = collectDescendantsOfType<KDocName> { it.getQualifiedName().firstOrNull() == shortName }
            if (inCode.isEmpty() && inDocs.isEmpty()) removeImport(id.asSingleFqName().asString())
        }
    }

    protected fun lineStartOffsets(lines: List<String>): IntArray {
        val offsets = IntArray(lines.size)
        for (index in 1 until lines.size) offsets[index] = offsets[index - 1] + lines[index - 1].length + 1
        return offsets
    }

    protected fun KtFile.addImport(fqName: String) {
        if (packageFqName.asString() == fqName.substringBeforeLast('.')) return
        val line = "import $fqName"
        val lines = text.lines()
        val block = ImportOrdering.createImportBlocks(lines).firstOrNull { !it.inPreprocessingBlock }
        if (block != null) {
            if (block.importLines.any { it.text == line }) return
            val starts = lineStartOffsets(lines)
            val next = block.importLines.firstOrNull { ImportOrdering.getOrdering().compare(it.text, line) > 0 }
            if (next != null) {
                correct(this, starts[next.lineIndex], starts[next.lineIndex], "$line\n")
                return
            }
            val last = block.importLines.last()
            val end = starts[last.lineIndex] + lines[last.lineIndex].length
            correct(this, end, end, "\n$line")
            return
        }
        val anchor = packageDirective?.takeIf { it.textLength > 0 } ?: fileAnnotationList?.takeIf { it.textLength > 0 }
        if (anchor == null) {
            correct(this, 0, 0, "$line\n\n")
            return
        }
        correct(this, anchor.textRange.endOffset, anchor.textRange.endOffset, "\n\n$line")
    }

    private fun PsiElement.isSuppressed(): Boolean {
        val ids = setOf(ruleName.value, "all")
        return parentsWithSelf.filterIsInstance<KtAnnotated>().any { annotated ->
            annotated.annotationEntries.any { entry ->
                entry.shortName?.asString() in SUPPRESS_ANNOTATIONS && entry.valueArguments.any { argument ->
                    val id = argument.getArgumentExpression()?.text?.trim('"').orEmpty()
                    id.substringAfterLast(':').removePrefix("detekt.") in ids
                }
            }
        }
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

    companion object {
        val CONVENTIONS_URI = URI("https://github.com/SkyHanniStudios/SkyHanni/blob/beta/CONTRIBUTING.md#coding-styles-and-conventions")

        private val SUPPRESS_ANNOTATIONS = setOf(Suppress::class.java.simpleName, SuppressWarnings::class.java.simpleName)
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
