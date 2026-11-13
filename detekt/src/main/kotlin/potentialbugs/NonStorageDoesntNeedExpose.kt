package potentialbugs

import SkyHanniAnalysisRule
import com.google.gson.annotations.Expose
import dev.detekt.api.Config
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import utils.DetektUtils.getAnnotation
import utils.DetektUtils.hasAnnotation
import utils.DetektUtils.isConfigOrStorageFile

class NonStorageDoesntNeedExpose(config: Config) : SkyHanniAnalysisRule(
    config,
    "Config and storage properties that do not store data should not be annotated with @Expose.",
) {

    override fun visitKtFile(file: KtFile) {
        if (file.isConfigOrStorageFile()) super.visitKtFile(file)
    }

    override fun visitProperty(property: KtProperty) {
        val expose = property.getAnnotation<Expose>()
        if (expose != null) {
            // Reasons to NOT have Expose annotation:
            //  - The property is local
            //  - The property is private
            //  - The property has a getter
            //  - The property is annotated with ConfigEditorInfoText
            //  - The property is annotated with ConfigEditorButton
            //  - The property is annotated with Transient
            val hasExplicitGetter = property.getter?.hasBody() ?: false
            val doWeCare = property.isLocal || property.isPrivate() || hasExplicitGetter ||
                property.hasAnnotation<ConfigEditorInfoText>() ||
                property.hasAnnotation<ConfigEditorButton>() ||
                property.hasAnnotation<Transient>()

            if (doWeCare) {
                expose.reportIssue("@Expose annotation is not needed on property ${property.name}", property)
                if (expose.valueArgumentList == null) expose.deleteText()
            }
        }

        return super.visitProperty(property)
    }
}
