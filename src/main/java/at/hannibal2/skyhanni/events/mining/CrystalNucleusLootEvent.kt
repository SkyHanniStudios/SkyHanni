package at.hannibal2.skyhanni.events.mining

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.utils.NeuInternalName

@PrimaryFunction("onCrystalNucleusLoot")
class CrystalNucleusLootEvent(val loot: Map<NeuInternalName, Int>) : SkyHanniEvent()
