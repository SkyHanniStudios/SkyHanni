package potentialbugs

import SkyHanniAnalysisRule
import com.google.gson.annotations.Expose
import dev.detekt.api.Config
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty
import utils.DetektUtils.doWeNeedToCheckConfigProp
import utils.DetektUtils.hasAnnotation
import utils.DetektUtils.isConfigOrStorageFile
import utils.DetektUtils.javaClassId

class StorageNeedsExpose(config: Config) : SkyHanniAnalysisRule(
    config,
    "Config and storage properties that store data should be annotated with @Expose.",
) {

    override fun visitKtFile(file: KtFile) {
        if (file.isConfigOrStorageFile()) super.visitKtFile(file)
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
        property.addAnnotation(Expose::class.javaClassId())
    }

    override fun visitProperty(property: KtProperty) {
        checkProperty(property)
        super.visitProperty(property)
    }
}
