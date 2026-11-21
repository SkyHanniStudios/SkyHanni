package at.hannibal2.skyhanni.events.dungeon

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

// TODO refactor to DungeonEvents.kt with others
//  also while I'm here I figured I'd note that using a string for floor,
//  while we have an enum for it [at.hannibal2.skyhanni.features.dungeon.DungeonFloor],
//  is pretty peak
@PrimaryFunction("onDungeonEnter")
class DungeonEnterEvent(val dungeonFloor: String) : SkyHanniEvent()
