package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.compat.appendWithColor
import at.hannibal2.skyhanni.utils.compat.bold
import at.hannibal2.skyhanni.utils.compat.componentBuilder

data class SeaCreature(
    val name: String,
    val fishingExperience: Int,
    val chatColor: String,
    val rare: Boolean,
    val rarity: LorenzRarity,
    val lootshareSphereOverride: Boolean?,
    val oldNames: List<String> = emptyList(),
    val rgbColor: Int,
) {

    val displayName = chatColor + legacyRare() + name

    val componentDisplayName = componentBuilder {
        appendWithColor(string = name, color = rgbColor) {
            bold = rare
        }
    }

    private fun legacyRare() = if (rare) "§l" else ""
}

