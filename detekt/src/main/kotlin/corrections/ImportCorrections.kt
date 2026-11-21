package corrections

import com.intellij.psi.PsiElement
import imports.ImportOrdering
import org.jetbrains.kotlin.kdoc.psi.impl.KDocName
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isAncestor

interface ImportCorrections : TextCorrections {

    fun KtFile.addImport(classId: ClassId) {
        if (packageFqName == classId.packageFqName && !classId.isNestedClass) return
        val line = "import ${classId.asSingleFqName().asString()}"
        val lines = text.lines()
        val block = ImportOrdering.createImportBlocks(lines).firstOrNull { !it.inPreprocessingBlock }
        if (block != null) {
            if (block.importLines.any { it.text == line }) return
            val starts = OffsetMapper.lineStartOffsets(lines)
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

    fun KtFile.removeImport(classId: ClassId): Boolean {
        val fqName = classId.asSingleFqName()
        val directive = importDirectives.firstOrNull { it.importedFqName == fqName && it.aliasName == null } ?: return false
        return directive.deleteText()
    }

    fun KtFile.removeImportIfUnused(classId: ClassId, removed: PsiElement) {
        listOf(classId, classId.outermostClassId).distinct().forEach { id ->
            val shortName = id.shortClassName
            val inCode = collectDescendantsOfType<KtNameReferenceExpression> {
                it.getReferencedNameAsName() == shortName &&
                    it.getStrictParentOfType<KtImportDirective>() == null &&
                    !removed.isAncestor(it)
            }
            val inDocs = collectDescendantsOfType<KDocName> { it.getQualifiedName().firstOrNull() == shortName.asString() }
            if (inCode.isEmpty() && inDocs.isEmpty()) removeImport(id)
        }
    }
}
