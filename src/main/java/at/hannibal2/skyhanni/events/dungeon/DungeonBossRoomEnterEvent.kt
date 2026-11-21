package at.hannibal2.skyhanni.events.dungeon

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

// TODO refactor to DungeonEvents.kt with others
@PrimaryFunction("onDungeonBossRoomEnter")
object DungeonBossRoomEnterEvent : SkyHanniEvent()
