package repo

import SkyHanniRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

/**
 * This rule reports all instances of hard-coded skull textures in the codebase.
 */
class SkullTexturesUseRepo(config: Config) : SkyHanniRule(
    config,
    "Skull textures should come from SkullTextureHolder and Skulls.json in the repo instead of being hard-coded.",
) {

    private val scannedTextureStarts = listOf(
        "ewogICJ0aW1l",
        "eyJ0ZXh0dXJl",
    )

    override fun visitStringTemplateExpression(expression: KtStringTemplateExpression) {
        val text = expression.entries.joinToString("") { it.text }
        if (scannedTextureStarts.any { text.startsWith(it) }) {
            expression.reportIssue("Avoid hard-coding skull texture text in strings.")
        }
        super.visitStringTemplateExpression(expression)
    }
}
