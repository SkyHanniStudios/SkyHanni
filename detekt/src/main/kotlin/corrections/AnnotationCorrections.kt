package corrections

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.psiUtil.siblings

interface AnnotationCorrections : ImportCorrections {

    fun KtDeclaration.addAnnotation(classId: ClassId, arguments: String? = null): Boolean {
        val anchor = modifierList?.takeIf { it.textLength > 0 }
            ?: firstChild?.siblings()?.firstOrNull { it !is PsiWhiteSpace && it !is PsiComment }
            ?: return false
        if (!canCorrect(anchor)) return false
        val text = containingFile.text
        val lineStart = text.lastIndexOf('\n', anchor.textRange.startOffset - 1) + 1
        val indent = text.substring(lineStart, anchor.textRange.startOffset)
        val separator = if (indent.isBlank()) "\n$indent" else " "
        val argumentList = arguments?.let { "($it)" }.orEmpty()
        anchor.insertTextBefore("@${classId.shortClassName.asString()}$argumentList$separator")
        containingKtFile.addImport(classId)
        return true
    }

    fun KtAnnotationEntry.removeAnnotation(classId: ClassId): Boolean {
        if (!deleteText()) return false
        containingKtFile.removeImportIfUnused(classId, this)
        return true
    }

    fun KtAnnotationEntry.addArgument(argument: String): Boolean {
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

    fun KtAnnotationEntry.argumentNames(): List<String> =
        valueArguments.mapNotNull { it.getArgumentName()?.asName?.asString() }

    fun KtAnnotationEntry.namedArgument(name: String): KtValueArgument? =
        valueArgumentList?.arguments?.firstOrNull { it.getArgumentName()?.asName?.asString() == name }

    fun KtValueArgument.deleteArgument(): Boolean {
        val arguments = (parent as? KtValueArgumentList)?.arguments ?: return false
        val index = arguments.indexOf(this)
        return when {
            index > 0 -> correct(this, arguments[index - 1].textRange.endOffset, textRange.endOffset, "")
            arguments.size > 1 -> correct(this, textRange.startOffset, arguments[1].textRange.startOffset, "")
            else -> correct(this, textRange.startOffset, textRange.endOffset, "")
        }
    }

    fun KtValueArgument.removeArgument(): Boolean {
        val list = parent as? KtValueArgumentList ?: return false
        if (list.arguments.any { it != this && it.getArgumentName() == null }) return false
        return if (list.arguments.size == 1) list.replaceText("") else deleteArgument()
    }
}
