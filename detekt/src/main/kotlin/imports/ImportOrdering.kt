package imports

import PreprocessingPattern

object ImportOrdering {
    private val importOrder = Comparator(::compareImports)

    private val packageImportOrdering = listOf("java.", "javax.", "kotlin.", "kotlinx.")

    data class ImportLine(
        val text: String,
        val lineIndex: Int,
    )

    data class ImportBlock(
        val inPreprocessingBlock: Boolean,
        val importLines: List<ImportLine>,
    )

    fun getOrdering(): Comparator<String> = importOrder

    fun createImportBlocks(lines: List<String>): List<ImportBlock> {
        val blocks = mutableListOf<ImportBlock>()
        var currentImports = mutableListOf<ImportLine>()
        var inPreprocessingBlock = false
        var inImportSection = false

        fun flush() {
            if (currentImports.isNotEmpty()) {
                blocks += ImportBlock(inPreprocessingBlock, currentImports)
                currentImports = mutableListOf()
            }
        }

        fun isImportLine(line: String): Boolean =
            line.startsWith("import ") ||
                line.startsWith("/*import ")

        for ((index, rawLine) in lines.withIndex()) {
            val line = rawLine.trim()

            when {
                line.startsWith("package ") -> continue

                line.isBlank() -> continue

                PreprocessingPattern.IF.matches(line) -> {
                    inImportSection = true
                    flush()
                    inPreprocessingBlock = true
                }

                PreprocessingPattern.ELSEIF.matches(line) ||
                    PreprocessingPattern.ELSE.matches(line) -> {
                    flush()
                    inPreprocessingBlock = true
                }

                PreprocessingPattern.ENDIF.matches(line) -> {
                    flush()
                    inPreprocessingBlock = false
                }

                isImportLine(line) -> {
                    inImportSection = true

                    currentImports += ImportLine(
                        line.removePrefix("/*"),
                        index,
                    )
                }

                inImportSection -> {
                    flush()
                    break
                }
            }
        }

        flush()

        return blocks
    }

    private fun compareImports(import1: String, import2: String): Int {
        val path1 = import1.removePrefix("import ").substringBefore(" as ")
        val path2 = import2.removePrefix("import ").substringBefore(" as ")

        val alias1 = import1.contains(" as ")
        val alias2 = import2.contains(" as ")

        val index1 = packageImportOrdering.indexOfFirst(path1::startsWith)
        val index2 = packageImportOrdering.indexOfFirst(path2::startsWith)

        return when {
            alias1 && alias2 -> path1.compareTo(path2)
            alias1 -> 1
            alias2 -> -1
            index1 == -1 && index2 == -1 -> path1.compareTo(path2)
            index1 == -1 -> -1
            index2 == -1 -> 1
            index1 != index2 -> index1.compareTo(index2)
            else -> path1.compareTo(path2)
        }
    }
}
