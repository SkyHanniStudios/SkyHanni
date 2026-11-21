package at.hannibal2.skyhanni.test.framework.fixtures

import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.test.framework.TestFixture
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraft.network.chat.Component
import org.junit.jupiter.api.extension.ExtensionContext

class OutputFixture : TestFixture {
    val chat = mutableListOf<Component>()
    val errors = mutableListOf<ErrorManager.LoggedError>()

    val chatLines: List<String> get() = chat.map { it.string.removeColor() }

    override fun setUp(context: ExtensionContext) {
        ChatUtils.testSink = { chat.add(it) }
        ErrorManager.testSink = { errors.add(it) }
    }

    override fun tearDown(context: ExtensionContext) {
        ChatUtils.testSink = null
        ErrorManager.testSink = null
    }
}
