package at.hannibal2.skyhanni.events.garden.farming

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.contest.FarmingContestPhase
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

// TODO Refactor to `FarmingContestEvent`, with `START`, `STOP`, and `CHANGE` sub-events.
@PrimaryFunction("onFarmingContest")
class FarmingContestEvent(val crop: CropType, val phase: FarmingContestPhase) : SkyHanniEvent()
