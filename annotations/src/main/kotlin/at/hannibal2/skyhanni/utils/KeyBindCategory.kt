package at.hannibal2.skyhanni.utils

enum class KeyBindCategory {
    GENERAL,
    INVENTORY,
    GARDEN,
    FISHING,
    MINING,
    COMBAT,
    DEV,
    ;

    val id: String = name.lowercase()
}
