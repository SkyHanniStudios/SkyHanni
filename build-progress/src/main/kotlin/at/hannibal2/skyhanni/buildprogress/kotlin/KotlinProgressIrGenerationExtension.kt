package at.hannibal2.skyhanni.buildprogress.kotlin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class KotlinProgressIrGenerationExtension(private val tracker: KotlinProgressTracker) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) =
        tracker.loweringStarted(moduleFragment.files.size)
}
