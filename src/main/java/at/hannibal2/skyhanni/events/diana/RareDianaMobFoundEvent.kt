package at.hannibal2.skyhanni.events.diana

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import net.minecraft.client.player.RemotePlayer

@PrimaryFunction("onRareDianaMobFound")
class RareDianaMobFoundEvent(val entity: RemotePlayer) : SkyHanniEvent()
