package at.hannibal2.skyhanni.utils

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class RegisterKeyBind(
    val name: String,
    val category: KeyBindCategory = KeyBindCategory.GENERAL,
    val context: KeyBindContext = KeyBindContext.IN_GAME,
    val trigger: KeyBindTrigger = KeyBindTrigger.PRESS,
)
