package at.hannibal2.skyhanni.buildprogress.kotlin

import org.jetbrains.kotlin.backend.jvm.extensions.ClassGenerator
import org.jetbrains.kotlin.backend.jvm.extensions.ClassGeneratorExtension
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.fileOrNull

class KotlinProgressClassGeneratorExtension(private val tracker: KotlinProgressTracker) : ClassGeneratorExtension {

    override fun generateClass(generator: ClassGenerator, declaration: IrClass?): ClassGenerator {
        tracker.classGenerated(declaration?.fileOrNull?.fileEntry?.name)
        return generator
    }
}
