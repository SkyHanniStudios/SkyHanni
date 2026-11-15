package at.hannibal2.skyhanni.utils

class KeyBindRegistration(
    val owner: Class<*>,
    val keyBind: () -> SkyHanniKeyBind,
    val mappingKey: String,
    val categoryPath: String,
    val inGame: Boolean,
    val inScreen: Boolean,
    val hold: Boolean,
)
