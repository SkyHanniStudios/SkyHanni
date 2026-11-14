package corrections

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

interface TextCorrections {

    fun canCorrect(anchor: PsiElement): Boolean

    fun correct(anchor: PsiElement, start: Int, end: Int, replacement: String): Boolean

    fun PsiElement.replaceText(replacement: String): Boolean =
        correct(this, textRange.startOffset, textRange.endOffset, replacement)

    fun PsiElement.insertTextBefore(text: String): Boolean =
        correct(this, textRange.startOffset, textRange.startOffset, text)

    fun PsiElement.ownLines(): TextRange? {
        val text = containingFile.text
        val start = text.lastIndexOf('\n', textRange.startOffset - 1) + 1
        val end = text.indexOf('\n', textRange.endOffset).let { if (it == -1) text.length else it + 1 }
        val alone = text.substring(start, textRange.startOffset).isBlank() && text.substring(textRange.endOffset, end).isBlank()
        return TextRange(start, end).takeIf { alone }
    }

    fun PsiElement.deleteText(withFollowingBlankLine: Boolean = false): Boolean {
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
}
