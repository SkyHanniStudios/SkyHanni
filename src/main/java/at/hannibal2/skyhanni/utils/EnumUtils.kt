package at.hannibal2.skyhanni.utils

import kotlin.enums.enumEntries

object EnumUtils {

    inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String): T? = enumEntries<T>().firstOrNull { it.name == name }

    inline fun <reified T : Enum<T>> enumValueOf(name: String) =
        enumValueOfOrNull<T>(name) ?: error("Unknown enum constant for ${T::class.simpleName}: '$name'")

    inline fun <reified T : Enum<T>> enumJoinToPattern(noinline transform: (T) -> CharSequence = { it.name }) =
        enumEntries<T>().joinToString("|", transform = transform)

    inline fun <reified T : Enum<T>> T.isAnyOf(vararg array: T): Boolean = array.contains(this)

    fun <T : Enum<T>> Enum<T>.toFormattedName(): String =
        name.split("_").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }

    inline fun <reified T : Enum<T>> T.previous(wrap: Boolean = false): T? {
        val values = enumEntries<T>()
        val previousIndex = ordinal - 1
        return when {
            previousIndex >= 0 -> values[previousIndex]
            wrap -> values.lastOrNull()
            else -> null
        }
    }

    inline fun <reified T : Enum<T>> T.next(wrap: Boolean = false): T? {
        val values = enumEntries<T>()
        val nextIndex = ordinal + 1
        return when {
            nextIndex < values.size -> values[nextIndex]
            wrap -> values[0]
            else -> null
        }
    }

}
