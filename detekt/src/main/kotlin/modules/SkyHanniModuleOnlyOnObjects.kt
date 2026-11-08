package modules

import SkyHanniAnalysisRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtClass
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.hasAnnotation
import utils.SkyHanniSymbols.isInSkyHanniPackage

class SkyHanniModuleOnlyOnObjects(config: Config) : SkyHanniAnalysisRule(
    config,
    "@SkyHanniModule is only valid on objects, because LoadedModules registers the singleton instance.",
) {

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        if (klass.name == null || !klass.isInSkyHanniPackage()) return
        val annotated = analyze(klass) { klass.classSymbol?.hasAnnotation(SkyHanniFqNames.SKYHANNI_MODULE_ID) == true }
        if (annotated) (klass.nameIdentifier ?: klass).reportIssue("@SkyHanniModule can only be applied to objects.", klass)
    }
}
