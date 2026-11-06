package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod.async
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigFileType
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.config.commands.brigadier.BrigadierArguments
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.coroutines.CoroutineSettings
import kotlinx.coroutines.Deferred

//? if >= 26.3 {
import org.lwjgl.sdl.SDLError
import java.lang.foreign.Arena
import net.minecraft.util.StringDecomposer
import org.lwjgl.sdl.SDLClipboard
//?} else {
/*import com.mojang.blaze3d.platform.ClipboardManager
import net.minecraft.client.Minecraft
*///?}

@SkyHanniModule
object ClipboardUtils {
    private val clipboardCoroutineSettings = CoroutineSettings(
        "clipboardAccess",
        withIOContext = true,
    )

    private const val MAX_ATTEMPTS = 4

    @Deprecated("Use copyToClipboardAsync instead", ReplaceWith("copyToClipboardAsync(text).await()"))
    fun copyToClipboard(text: String) = copyToClipboardInternal(text)

    fun copyToClipboardAsync(text: String): Deferred<Boolean?> = clipboardCoroutineSettings.async {
        copyToClipboardInternal(text)
    }

    private fun copyToClipboardInternal(text: String): Boolean = retryClipboardOperation(
        operation = {
            //? if >= 26.3 {
            // Doesn't use SDL_SetClipboardText due to out of stack memory issues with large text
            Arena.ofConfined().use { arena ->
                val memory = arena.allocateFrom(text)
                if (!SDLClipboard.nSDL_SetClipboardText(memory.address())) {
                    throw IllegalStateException(
                        "Failed to set clipboard text: ${SDLError.SDL_GetError() ?: "Unknown SDL error"}",
                    )
                }
            }
            //?} else {
            /*ClipboardManager().setClipboard(
                Minecraft.getInstance().window,
                text,
            )
            *///?}
            true
        },
        onFailure = {
            ErrorManager.logErrorWithData(it)
            false
        },
    )

    fun readFromClipboard(): String? = retryClipboardOperation(
        operation = {
            //? if >= 26.3 {
            val rawClipboard = SDLClipboard.SDL_GetClipboardText() ?: run {
                throw IllegalStateException("Failed to get clipboard text: ${SDLError.SDL_GetError() ?: "Unknown SDL error"}")
            }
            val clipboard = StringDecomposer.filterBrokenSurrogates(rawClipboard)
            //?} else {
            /*var glfwError: Throwable? = null

            val clipboard = ClipboardManager().getClipboard(
                Minecraft.getInstance().window
            ) { error, description ->
                glfwError = IllegalStateException(
                    "Failed to read clipboard (GLFW error $error): $description"
                )
            }

            glfwError?.let { throw it }
            *///?}
            clipboard
        },
        onFailure = {
            ErrorManager.logErrorWithData(it)
            null
        },
    )

    private fun <T> retryClipboardOperation(
        operation: () -> T,
        onFailure: (Throwable) -> T,
    ): T {
        repeat(MAX_ATTEMPTS - 1) {
            try {
                return operation()
            } catch (_: Throwable) {
                // Retry
            }
        }

        return try {
            operation()
        } catch (e: Throwable) {
            onFailure(e)
        }
    }

    @HandleEvent
    private fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.registerBrigadier("shtestclipboard") {
            description = "Test clipboard read/write"
            category = DEVELOPER_TEST
            literal("copy") {
                literal("config") {
                    coroutineSimpleCallback {
                        val config = ConfigFileType.FEATURES.file.readText()
                        copyToClipboardAsync(config).await()
                        ChatUtils.chat("Copied config to clipboard!")
                    }
                }
                literal("input") {
                    arg("text", BrigadierArguments.greedyString()) { textArg ->
                        coroutineSimpleCallback {
                            val text = getArg(textArg)
                            copyToClipboardAsync(text).await()
                            ChatUtils.chat("Copied config to clipboard!")
                        }
                    }
                }
            }
            literal("paste") {
                coroutineSimpleCallback {
                    val clipboardText = readFromClipboard()
                    ChatUtils.chat("Clipboard contents:")
                    ChatUtils.chat(
                        clipboardText ?: "Failed to read clipboard",
                        prefix = false,
                    )
                }
            }
        }
    }
}
