package corrections

class CorrectionBuffer {

    private data class Correction(val start: Int, val end: Int, val replacement: String)

    data class Result(val text: String, val skipped: Int)

    private val pending = mutableListOf<Correction>()

    val isEmpty: Boolean get() = pending.isEmpty()

    fun add(start: Int, end: Int, replacement: String): Boolean {
        if (start < 0 || end < start) return false
        pending += Correction(start, end, replacement)
        return true
    }

    fun clear() = pending.clear()

    fun rebase(mapper: OffsetMapper) {
        val rebased = pending.mapNotNull { correction ->
            mapper.map(correction.start, correction.end)?.let { Correction(it.startOffset, it.endOffset, correction.replacement) }
        }
        pending.clear()
        pending += rebased
    }

    fun applyTo(text: String): Result {
        val builder = StringBuilder(text)
        var limit = text.length
        var skipped = 0
        pending.distinct()
            .sortedWith(compareByDescending<Correction> { it.start }.thenByDescending { it.end })
            .forEach { correction ->
                if (correction.end > limit) {
                    skipped++
                    return@forEach
                }
                builder.replace(correction.start, correction.end, correction.replacement)
                limit = correction.start
            }
        return Result(builder.toString(), skipped)
    }
}
