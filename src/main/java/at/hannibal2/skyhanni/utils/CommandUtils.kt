package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.NeuItems.isGenerator

object CommandUtils {

    private enum class NameSource {
        INTERNAL_NAME,
        ITEM_NAME,
    }

    private val namePattern = "^(?i)(name:)(.*)".toRegex()
    private val internalPattern = "^(?i)(internal:)(.*)".toRegex()

    fun itemTabComplete(
        start: String,
        validItems: (NeuInternalName) -> Boolean = { !it.isGenerator() },
        aliases: Map<String, NeuInternalName> = NeuItems.commonItemAliases.global,
        suggestAtEmpty: Boolean = false,
    ): List<String> = buildList {
        if (!suggestAtEmpty && start.isEmpty()) return@buildList
        val expected = when {
            namePattern.matches(start) -> NameSource.ITEM_NAME
            internalPattern.matches(start) -> NameSource.INTERNAL_NAME
            else -> null
        }

        val uppercaseStart = start.uppercase().replace(" ", "_")
        val lowercaseStart = start.lowercase().replace("_", " ")

        fun MutableList<String>.resultAdd(
            pattern: Regex,
            start: String,
            transformedStart: String,
            similar: (String) -> Collection<String>,
        ) {
            val prefix = start.replace(pattern, "$1")
            val withoutPrefix = transformedStart.replace(pattern, "$2")
            if (withoutPrefix.isEmpty()) return
            val lastSpaceIndex = start.replace(pattern, "$2").indexOfLast { it == ' ' } + 1
            this@resultAdd.addAll(
                similar(withoutPrefix).map { result ->
                    if (lastSpaceIndex == 0) {
                        prefix + result
                    } else {
                        result.substring(lastSpaceIndex)
                    }
                },
            )
        }

        when (expected) {
            NameSource.INTERNAL_NAME -> resultAdd(
                internalPattern,
                start,
                uppercaseStart,
            ) { NeuItems.findInternalNameStartingWithWithoutNPCs(it, validItems) }

            NameSource.ITEM_NAME -> resultAdd(
                namePattern,
                start,
                lowercaseStart,
            ) { NeuItems.findItemNameStartingWithWithoutNPCs(it, validItems) }

            null -> {
                val lastSpaceIndex = start.indexOfLast { it == ' ' } + 1
                addAll(aliases.filter { it.key.startsWith(lowercaseStart) }.filter { validItems(it.value) }.keys)
                addAll(NeuItems.findInternalNameStartingWithWithoutNPCs(uppercaseStart, validItems).map { it.substring(lastSpaceIndex) })
                // 200 is here to limit the max amount of results since more than that can introduce performance issues for the client
                if (size < 200) {
                    addAll(
                        NeuItems.findItemNameStartingWithWithoutNPCs(lowercaseStart, validItems).map { result ->
                            result.substring(lastSpaceIndex).replace(" ", "_")
                        },
                    )
                }
            }
        }
    }
}
