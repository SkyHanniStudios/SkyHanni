package at.hannibal2.skyhanni.events.dungeon

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.ClickedBlockType
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.utils.LorenzVec

// TODO refactor to DungeonEvents.kt with others
@PrimaryFunction("onDungeonBlockClick")
class DungeonBlockClickEvent(val position: LorenzVec, val blockType: ClickedBlockType) : SkyHanniEvent()

