package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.network.chat.Component

// TODO, I'm redoing all of these already. -Dae
@Suppress("MissingPrimaryFunction")
class EntityHealthDisplayEvent(var text: Component) : SkyHanniEvent()
