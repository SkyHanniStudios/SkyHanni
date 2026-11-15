package at.hannibal2.skyhanni.utils

enum class KeyBindContext(val inGame: Boolean, val inScreen: Boolean) {
    IN_GAME(inGame = true, inScreen = false),
    IN_SCREEN(inGame = false, inScreen = true),
    ALL(inGame = true, inScreen = true),
}
