package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.SafeItemStack
import at.hannibal2.skyhanni.utils.vector.SkyHanniVector

class MinionOpenEvent(val inventoryName: String, val inventoryItems: Map<Int, SafeItemStack>) : SkyHanniEvent()
class MinionCloseEvent : SkyHanniEvent()
class MinionStorageOpenEvent(val position: SkyHanniVector?, val inventoryItems: Map<Int, SafeItemStack>) : SkyHanniEvent()
