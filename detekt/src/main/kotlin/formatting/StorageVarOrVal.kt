package formatting

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty
import utils.DetektUtils.doWeNeedToCheckConfigProp
import utils.DetektUtils.hasAnnotation
import utils.DetektUtils.javaClassId
import utils.SkyHanniFqNames

class StorageVarOrVal(config: Config) : SkyHanniAnalysisRule(
    config,
    "Storage and config properties should be `var` for primitives, strings, enums and colors, and `val` for other objects.",
) {
    private enum class StorageType { VAR, VAL }

    companion object {
        private val CHROMA_COLOUR_CLASS_ID = ChromaColour::class.javaClassId()
        private val PRIMITIVE_CLASS_IDS = with(StandardClassIds) {
            setOf(Boolean, Byte, Char, Double, Float, Int, Long, Short)
        }
    }

    override fun visitKtFile(file: KtFile) {
        if (file.packageFqName.startsWith(SkyHanniFqNames.CONFIG_FEATURES_PACKAGE)) super.visitKtFile(file)
    }

    override fun visitProperty(property: KtProperty) {
        if (!property.doWeNeedToCheckConfigProp()) return

        val expected = if (shouldBeVar(property)) StorageType.VAR else StorageType.VAL
        val actual = if (property.isVar) StorageType.VAR else StorageType.VAL

        if (actual != expected) {
            val keyword = property.valOrVarKeyword
            keyword.reportIssue("${property.typeReference?.text} `${property.name}` should be a ${expected.name.lowercase()}", property)
            keyword.replaceText(expected.name.lowercase())
            return
        }

        super.visitProperty(property)
    }

    private fun shouldBeVar(property: KtProperty): Boolean {
        val isInfoText = property.hasAnnotation<ConfigEditorInfoText>()
        return !isInfoText && analyze(property) {
            val type = property.returnType
            val classId = (type as? KaClassType)?.classId
            classId in PRIMITIVE_CLASS_IDS ||
                classId == StandardClassIds.String ||
                type.expandedSymbol?.classKind == KaClassKind.ENUM_CLASS ||
                classId == CHROMA_COLOUR_CLASS_ID
        }
    }
}
