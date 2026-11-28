package at.hannibal2.skyhanni.events.minecraft

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import com.mojang.blaze3d.platform.InputConstants

/**
 * Base class for keyboard input events.
 *
 * @param key the input code associated with the event
 */
abstract class AbstractKeyEvent(val key: InputConstants.Key) : SkyHanniEvent() {
    override fun toString(): String {
        return "${this::class.simpleName}(key=${key.name})"
    }
}

/** Fired once when a key is pressed down. Use this for taps. */
@PrimaryFunction("onKeyDown")
class KeyDownEvent(key: InputConstants.Key) : AbstractKeyEvent(key)

/** Fired each tick while a key is held down. */
@PrimaryFunction("onKeyPress")
class KeyPressEvent(key: InputConstants.Key) : AbstractKeyEvent(key)

/** Fired when a key is released. */
@PrimaryFunction("onKeyUp")
class KeyUpEvent(key: InputConstants.Key) : AbstractKeyEvent(key)
