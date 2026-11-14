package compat

import SkyHanniRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

class MinecraftCompat(config: Config) : SkyHanniRule(
    config,
    "Use MinecraftCompat instead of reading the player or world from Minecraft.getMinecraft() directly.",
) {

    @Suppress("MinecraftCompat")
    private val replacements = mapOf(
        "Minecraft.getMinecraft().thePlayer" to "MinecraftCompat.localPlayer",
        "Minecraft.getMinecraft().theWorld" to "MinecraftCompat.world",
    )

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val path = expression.containingKtFile.virtualFilePath.replace('\\', '/')
        if (path.contains("at/hannibal2/skyhanni/utils/compat")) return
        val text = expression.text.filterNot(Char::isWhitespace)
        val replacement = replacements[text] ?: return
        expression.reportIssue("Usage of $text detected. Please replace this with `$replacement` instead.")
    }
}
