package at.hannibal2.skyhanni.events.minecraft

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import com.mojang.blaze3d.platform.InputConstants

/** Counterpart to [KeyDownEvent]*/
@PrimaryFunction("onKeyUp")
class KeyUpEvent(val key: InputConstants.Key) : SkyHanniEvent()
