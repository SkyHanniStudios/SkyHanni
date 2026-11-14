package at.hannibal2.skyhanni.events.hoppity

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityApi
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

@PrimaryFunction("onRabbitFound")
class RabbitFoundEvent(
    val eggType: HoppityEggType,
    val duplicate: Boolean,
    val rabbitName: String,
    val chocGained: Long = 0,
) : SkyHanniEvent() {

    constructor(dataSet: HoppityApi.HoppityStateDataSet) : this(
        dataSet.lastMeal ?: HoppityEggType.BREAKFAST,
        dataSet.duplicate,
        dataSet.lastName,
        dataSet.lastDuplicateAmount ?: 0,
    )

    override fun toString(): String = buildString {
        appendLine("§fType§7: ${eggType.coloredName}")
        appendLine("§fDuplicate§7: §b$duplicate")
        appendLine("§fRabbit§7: $rabbitName")
        appendLine("§fChoc Gained§7: §6$chocGained")
    }
}
