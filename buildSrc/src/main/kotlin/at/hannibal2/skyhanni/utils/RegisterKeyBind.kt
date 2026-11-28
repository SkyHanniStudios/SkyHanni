package at.hannibal2.skyhanni.utils

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class RegisterKeyBind(
    val name: String,
    val category: KeyBindCategory = KeyBindCategory.GENERAL,
    val context: KeyBindContext = KeyBindContext.IN_GAME,
    val trigger: KeyBindTrigger = KeyBindTrigger.PRESS,
)
