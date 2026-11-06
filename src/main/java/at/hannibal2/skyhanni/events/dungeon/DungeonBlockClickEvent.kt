package at.hannibal2.skyhanni.events.dungeon

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.ClickedBlockType
import at.hannibal2.skyhanni.utils.vector.SkyHanniVector

class DungeonBlockClickEvent(val position: SkyHanniVector, val blockType: ClickedBlockType) : SkyHanniEvent()

