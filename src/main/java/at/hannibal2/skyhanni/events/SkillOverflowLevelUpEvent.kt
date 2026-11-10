package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.skillprogress.SkillType
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

@PrimaryFunction("onSkillOverflowLevelUp")
class SkillOverflowLevelUpEvent(val skill: SkillType, val oldLevel: Int, val newLevel: Int) : SkyHanniEvent()
