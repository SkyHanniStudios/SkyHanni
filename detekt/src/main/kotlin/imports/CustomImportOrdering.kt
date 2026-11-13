package imports

import SkyHanniRule
import corrections.OffsetMapper
import dev.detekt.api.Config
import imports.ImportOrdering.ImportBlock
import imports.ImportOrdering.ImportLine
import org.jetbrains.kotlin.KtPsiSourceFileLinesMapping
import org.jetbrains.kotlin.psi.KtFile

/**
 * This rule enforces correct import ordering, while taking preprocessed imports into account.
 */
class CustomImportOrdering(config: Config) : SkyHanniRule(
    config,
    "Imports should be sorted lexicographically, with Stonecutter-conditional import blocks last and separated by a blank line.",
) {

    // Preprocessed blocks must be at the end of the import list.
    private fun findPreprocessingBlockOrderViolation(
        blocks: List<ImportBlock>,
    ): ImportLine? {
        var preprocessingStarted = false

        for ((inPreprocessingBlock, importLines) in blocks) {
            if (inPreprocessingBlock) {
                preprocessingStarted = true
            } else if (preprocessingStarted) {
                return importLines.first()
            }
        }

        return null
    }

    // Must have empty lines between preprocessed and non-preprocessed blocks.
    private fun findSpacingBetweenBlocksViolations(
        fileLines: List<String>,
        blocks: List<ImportBlock>,
    ): List<Pair<ImportBlock, ImportBlock>> =
        blocks.zipWithNext().filter { (current, next) ->
            current.inPreprocessingBlock != next.inPreprocessingBlock &&
                fileLines
                    .subList(
                        current.importLines.last().lineIndex + 1,
                        next.importLines.first().lineIndex,
                    )
                    .none(String::isBlank)
        }

    // Must not have empty lines inside a block.
    private fun findSpacingInsideBlockViolations(
        fileLines: List<String>,
        blocks: List<ImportBlock>,
    ): List<ImportLine> =
        blocks.flatMap { block ->
            block.importLines.zipWithNext().mapNotNull { (current, next) ->
                if (
                    fileLines
                        .subList(
                            current.lineIndex + 1,
                            next.lineIndex,
                        )
                        .any(String::isBlank)
                ) {
                    next
                } else {
                    null
                }
            }
        }

    // Each block must be ordered according to the ordering defined in ImportOrdering.
    // Returns the first out of order import in order to not spam a lot of issues for the same block.
    private fun findOrderingViolations(
        blocks: List<ImportBlock>,
    ): List<ImportLine> =
        blocks.mapNotNull { block ->
            val imports = block.importLines.map { it.text }
            val sortedImports = imports.sortedWith(ImportOrdering.getOrdering())

            block.importLines.zip(sortedImports)
                .firstOrNull { (actual, expected) ->
                    actual.text != expected
                }
                ?.first
        }

    private fun correctBlock(file: KtFile, fileLines: List<String>, starts: IntArray, block: ImportBlock) {
        val first = block.importLines.first()
        val last = block.importLines.last()
        if (block.importLines.any { "*/" in it.text }) return
        val prefix = if (fileLines[first.lineIndex].trim().startsWith("/*")) "/*" else ""
        val sorted = block.importLines.map { it.text }.distinct().sortedWith(ImportOrdering.getOrdering())
        val replacement = prefix + sorted.joinToString("\n")
        val start = starts[first.lineIndex]
        val end = starts[last.lineIndex] + fileLines[last.lineIndex].length
        if (file.text.substring(start, end) != replacement) correct(file, start, end, replacement)
    }

    private fun correctSpacingBetween(file: KtFile, starts: IntArray, current: ImportBlock, next: ImportBlock) {
        val lineIndex = if (current.inPreprocessingBlock) next.importLines.first().lineIndex else current.importLines.last().lineIndex + 1
        correct(file, starts[lineIndex], starts[lineIndex], "\n")
    }

    override fun visitKtFile(file: KtFile) {
        val fileLines = file.text.lines()
        val blocks = ImportOrdering.createImportBlocks(fileLines)
        if (blocks.isEmpty()) {
            return
        }

        val sourceFileLinesMapping by lazy { KtPsiSourceFileLinesMapping(file) }
        val starts by lazy { OffsetMapper.lineStartOffsets(fileLines) }

        findPreprocessingBlockOrderViolation(blocks)?.let {
            reportIssue(
                "Preprocessed import blocks must be at the end of the import list.",
                it.lineIndex,
                fileLines[it.lineIndex],
                file,
                sourceFileLinesMapping,
            )
        }

        val insideSpacing = findSpacingInsideBlockViolations(fileLines, blocks)
        insideSpacing.forEach {
            reportIssue(
                "Import blocks must not contain empty lines between imports.",
                it.lineIndex,
                fileLines[it.lineIndex],
                file,
                sourceFileLinesMapping,
            )
        }

        findSpacingBetweenBlocksViolations(fileLines, blocks).forEach { (current, next) ->
            val line = next.importLines.first()
            reportIssue(
                "Preprocessed and non-preprocessed import blocks must be separated by an empty line.",
                line.lineIndex,
                fileLines[line.lineIndex],
                file,
                sourceFileLinesMapping,
            )
            if (canCorrect(file)) correctSpacingBetween(file, starts, current, next)
        }

        val ordering = findOrderingViolations(blocks)
        ordering.forEach {
            reportIssue(
                "Imports must be ordered in lexicographic order with \"java\", \"javax\", \"kotlin\", \"kotlinx\" and aliases in the end.",
                it.lineIndex,
                fileLines[it.lineIndex],
                file,
                sourceFileLinesMapping,
            )
        }

        if ((insideSpacing.isNotEmpty() || ordering.isNotEmpty()) && canCorrect(file)) {
            blocks.forEach { correctBlock(file, fileLines, starts, it) }
        }

        super.visitKtFile(file)
    }
}
