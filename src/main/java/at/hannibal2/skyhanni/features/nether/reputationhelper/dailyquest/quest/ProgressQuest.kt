package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest

import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.vector.SkyHanniVector

abstract class ProgressQuest(
    displayItem: NeuInternalName,
    location: SkyHanniVector?,
    questCategory: QuestCategory,
    displayName: String,
    state: QuestState,
    val needAmount: Int,
    var haveAmount: Int = 0,
) : Quest(displayItem, location, questCategory, displayName, state)
