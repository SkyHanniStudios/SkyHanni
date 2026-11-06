package at.hannibal2.skyhanni.test.framework

import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.reflect.KClass

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Fixture(val type: KClass<out TestFixture>)

interface TestFixture {
    fun setUp(context: ExtensionContext) = Unit
    fun tearDown(context: ExtensionContext) = Unit
}
