package at.hannibal2.skyhanni.events.dungeon

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonBossApi
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

// TODO refactor to DungeonEvents.kt with others
//  Alt; refactor to be an extension of MobEvent :^)
@PrimaryFunction("onDungeonBossPhaseChange")
class DungeonBossPhaseChangeEvent(val newPhase: DungeonBossApi.DungeonBossPhase) : SkyHanniEvent()
