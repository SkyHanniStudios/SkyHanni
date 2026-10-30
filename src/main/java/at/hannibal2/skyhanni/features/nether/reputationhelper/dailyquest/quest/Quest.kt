package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest

import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.vector.SkyHanniVector

abstract class Quest(
    val displayItem: NeuInternalName,
    val location: SkyHanniVector?,
    val category: QuestCategory,
    val internalName: String,
    var state: QuestState,
    val displayName: String = internalName,
)
