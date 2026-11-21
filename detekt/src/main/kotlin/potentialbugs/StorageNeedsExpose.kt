package potentialbugs

import SkyHanniRule
import com.google.gson.annotations.Expose
import dev.detekt.api.Config
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty
import utils.DetektUtils.doWeNeedToCheckConfigProp
import utils.DetektUtils.hasAnnotation

class StorageNeedsExpose(config: Config) : SkyHanniRule(
    config,
    "Config and storage properties that store data should be annotated with @Expose.",
) {

    companion object {
        const val STORAGE_PACKAGE = "at.hannibal2.skyhanni.config.storage"
        const val CONFIG_PACKAGE = "at.hannibal2.skyhanni.config.features"
    }

    override fun visitKtFile(file: KtFile) {
        val packageName = file.packageDirective?.fqName?.asString().orEmpty()
        if (!packageName.startsWith(CONFIG_PACKAGE) && !packageName.startsWith(STORAGE_PACKAGE)) return
        super.visitKtFile(file)
    }

    private fun checkProperty(property: KtProperty) {
        if (!property.doWeNeedToCheckConfigProp()) return
        if (property.hasAnnotation<Expose>()) return

        // If the property is not annotated with @Expose, report it
        if (property.hasAnnotation<ConfigOption>()) {
            // Valid reasons to not have the @Expose annotation on a config option:
            //  - Has the ConfigEditorInfoText annotation
            //  - Has the ConfigEditorButton annotation
            if (property.hasAnnotation<ConfigEditorInfoText>()) return
            if (property.hasAnnotation<ConfigEditorButton>()) return
        }

        (property.nameIdentifier ?: property).reportIssue("@Expose annotation is missing from property ${property.name}", property)
        addExpose(property)
    }

    private fun addExpose(property: KtProperty) {
        if (!canCorrect(property)) return
        val anchor = property.modifierList?.takeIf { it.textLength > 0 } ?: property.valOrVarKeyword
        val text = property.containingFile.text
        val lineStart = text.lastIndexOf('\n', anchor.textRange.startOffset - 1) + 1
        val indent = text.substring(lineStart, anchor.textRange.startOffset)
        val separator = if (indent.isBlank()) "\n$indent" else " "
        anchor.insertTextBefore("@${Expose::class.java.simpleName}$separator")
        property.containingKtFile.addImport(Expose::class.java.name)
    }

    override fun visitProperty(property: KtProperty) {
        checkProperty(property)
        super.visitProperty(property)
    }
}
