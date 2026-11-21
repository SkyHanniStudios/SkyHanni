package at.hannibal2.skyhanni.utils

object KeyBindIds {

    const val PAGE_SIZE = 20

    private val nonIdentifierChars = Regex("[^a-z0-9]+")

    fun mappingKey(category: KeyBindCategory, name: String): String =
        "key.skyhanni.${category.id}.${name.lowercase().replace(nonIdentifierChars, "_").trim('_')}"

    fun categoryPath(category: KeyBindCategory, page: Int): String = if (page == 0) category.id else "${category.id}_${page + 1}"

    fun categoryLabel(category: KeyBindCategory, page: Int): String {
        val label = "SkyHanni - ${category.id.replaceFirstChar(Char::uppercase)}"
        return if (page == 0) label else "$label ${page + 1}"
    }
}
