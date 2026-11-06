package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.test.framework.MinecraftBootstrap
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class MinecraftTestBootstrapExtension : BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) = MinecraftBootstrap.ensure()
}
