package corrections

import com.intellij.openapi.util.TextRange

class OffsetMapper(original: String, current: String) {

    private val originalLines = original.split('\n')
    private val currentLines = current.split('\n')
    private val originalStarts = lineStartOffsets(originalLines)
    private val currentStarts = lineStartOffsets(currentLines)
    private val lineMapping = matchLines()

    fun map(start: Int, end: Int): TextRange? {
        val firstLine = lineAt(start)
        val lastLine = lineAt(if (end > start) end - 1 else end)
        val shift = shiftOf(firstLine) ?: return null
        if ((firstLine..lastLine).any { shiftOf(it) != shift }) return null
        return TextRange(start + shift, end + shift)
    }

    private fun lineAt(offset: Int): Int = originalStarts.binarySearch(offset).let { if (it >= 0) it else -it - 2 }

    private fun shiftOf(line: Int): Int? =
        lineMapping.getOrNull(line)?.takeIf { it >= 0 }?.let { currentStarts[it] - originalStarts[line] }

    private fun matchLines(): IntArray {
        val mapping = IntArray(originalLines.size) { -1 }
        val shared = minOf(originalLines.size, currentLines.size)
        var prefix = 0
        while (prefix < shared && originalLines[prefix] == currentLines[prefix]) {
            mapping[prefix] = prefix
            prefix++
        }
        var suffix = 0
        val originalLast = originalLines.lastIndex
        val currentLast = currentLines.lastIndex
        while (suffix < shared - prefix && originalLines[originalLast - suffix] == currentLines[currentLast - suffix]) {
            mapping[originalLast - suffix] = currentLast - suffix
            suffix++
        }
        matchMiddle(prefix, originalLines.size - suffix, currentLines.size - suffix, mapping)
        return mapping
    }

    private fun matchMiddle(from: Int, originalEnd: Int, currentEnd: Int, mapping: IntArray) {
        val rows = originalEnd - from
        val columns = currentEnd - from
        if (rows <= 0 || columns <= 0 || rows.toLong() * columns > MAX_CELLS) return
        val width = columns + 1
        val lengths = IntArray((rows + 1) * width)
        for (row in rows - 1 downTo 0) {
            for (column in columns - 1 downTo 0) {
                lengths[row * width + column] = if (originalLines[from + row] == currentLines[from + column]) {
                    lengths[(row + 1) * width + column + 1] + 1
                } else {
                    maxOf(lengths[(row + 1) * width + column], lengths[row * width + column + 1])
                }
            }
        }
        var row = 0
        var column = 0
        while (row < rows && column < columns) {
            when {
                originalLines[from + row] == currentLines[from + column] -> {
                    mapping[from + row] = from + column
                    row++
                    column++
                }
                lengths[(row + 1) * width + column] >= lengths[row * width + column + 1] -> row++
                else -> column++
            }
        }
    }

    companion object {
        private const val MAX_CELLS = 4_000_000L

        fun lineStartOffsets(lines: List<String>): IntArray {
            val offsets = IntArray(lines.size)
            for (index in 1 until lines.size) offsets[index] = offsets[index - 1] + lines[index - 1].length + 1
            return offsets
        }
    }
}
