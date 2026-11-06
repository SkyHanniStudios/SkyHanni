package at.hannibal2.skyhanni.test.framework

import io.mockk.unmockkAll
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import kotlin.reflect.KClass

class SkyHanniTestExtension : BeforeAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

    override fun beforeAll(context: ExtensionContext) {
        if (context.requiredTestClass.hasFixture(MinecraftFixture::class)) MinecraftBootstrap.ensure()
    }

    override fun beforeEach(context: ExtensionContext) {
        val fixtures = context.requiredTestClass.fixtureTypes().map { it.java.getDeclaredConstructor().newInstance() }
        context.store().put(FIXTURES_KEY, fixtures)
        fixtures.forEach { it.setUp(context) }
    }

    override fun afterEach(context: ExtensionContext) {
        val fixtures = context.fixtures()
        try {
            fixtures.asReversed().forEach { it.tearDown(context) }
        } finally {
            unmockkAll()
            StaticReset.resetAll()
        }
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
        extensionContext.fixtures().any { parameterContext.parameter.type.isInstance(it) }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any =
        extensionContext.fixtures().first { parameterContext.parameter.type.isInstance(it) }

    companion object {
        private val NAMESPACE = ExtensionContext.Namespace.create(SkyHanniTestExtension::class.java)
        private const val FIXTURES_KEY = "fixtures"

        private fun ExtensionContext.store() = getStore(NAMESPACE)

        private fun ExtensionContext.fixtures(): List<TestFixture> =
            (store().get(FIXTURES_KEY) as? List<*>).orEmpty().filterIsInstance<TestFixture>()

        private fun Class<*>.hasFixture(type: KClass<out TestFixture>) = type in fixtureTypes()

        private fun Class<*>.fixtureTypes(): List<KClass<out TestFixture>> =
            generateSequence(this) { it.superclass }.toList().asReversed()
                .flatMap { it.annotations.mapNotNull { annotation -> annotation.annotationClass.java.getAnnotation(Fixture::class.java) } }
                .map { it.type }
                .distinct()
    }
}
