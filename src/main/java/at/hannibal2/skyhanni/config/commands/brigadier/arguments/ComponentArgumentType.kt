package at.hannibal2.skyhanni.config.commands.brigadier.arguments

import at.hannibal2.skyhanni.config.commands.brigadier.BrigadierUtils.escapeDoubleQuote
import at.hannibal2.skyhanni.config.commands.brigadier.BrigadierUtils.readGreedyString
import at.hannibal2.skyhanni.utils.json.SkyHanniTypeAdapter
import com.google.gson.JsonParser
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.network.chat.Component

class ComponentArgumentType(
    private val allowPlainText: Boolean,
) : ArgumentType<Component> {
    override fun parse(reader: StringReader): Component {
        val input = reader.readGreedyString().escapeDoubleQuote().trim()

        if (!input.matches(jsonRegex) && allowPlainText) {
            return Component.literal(input)
        }

        val parsedElement = runCatching { JsonParser.parseString(input) }.onFailure {
            throw INVALID_JSON.create()
        }.getOrNull()

        return runCatching {
            adapter.fromJsonTree(parsedElement)
        }.getOrNull() ?: throw INVALID_COMPONENT.create()
    }

    companion object {

        fun component(allowPlainText: Boolean = false) = ComponentArgumentType(allowPlainText)

        private val jsonRegex = "^[\\[{].".toRegex()
        private val INVALID_JSON = SimpleCommandExceptionType(Component.literal("Invalid JSON"))
        private val INVALID_COMPONENT = SimpleCommandExceptionType(Component.literal("Invalid component"))
        private val adapter get() = SkyHanniTypeAdapter.COMPONENT.typed<Component>()
    }
}
