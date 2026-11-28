package at.hannibal2.skyhanni.config.features.foraging

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.config.features.misc.tracker.individual.IndividualItemTrackerConfig
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ForagingTrackerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Track drops from foraging.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = true

    @Expose
    @ConfigLink(owner = ForagingTrackerConfig::class, field = "enabled")
    val position: Position = Position(-300, 300)

    enum class TreeGiftDisplayMode(val displayName: String) {
        FULL_GIFT("Full Gift"),
        COMPACT_GIFT("Compact Gift"),
        NO_GIFT("No Gift"),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(name = "Display Tree Gifts", desc = "Change how the Tree Gift messages are displayed in chat.")
    @ConfigEditorDropdown
    var displayTreeGifts: TreeGiftDisplayMode = TreeGiftDisplayMode.COMPACT_GIFT

    @Expose
    @ConfigOption(
        name = "Compact Gifts Bonus Drops",
        desc = "Lets you decide what types of bonus drops should be included in Compact Gifts messages."
    )
    @ConfigEditorDraggableList
    val compactGiftBonusDropsList: MutableList<TreeGiftBonusDropCategory> = mutableListOf(
        TreeGiftBonusDropCategory.UNCOMMON_DROPS,
        TreeGiftBonusDropCategory.ENCHANTED_BOOKS,
        TreeGiftBonusDropCategory.MOBS,
        TreeGiftBonusDropCategory.BOOSTERS,
        TreeGiftBonusDropCategory.SHARDS,
        TreeGiftBonusDropCategory.RUNES,
        TreeGiftBonusDropCategory.MISC,
    )

    @Suppress("MaxLineLength")
    enum class TreeGiftBonusDropCategory(private val displayName: String) {
        UNCOMMON_DROPS("§fUncommon Tree-Specific Drops\n§r(e.g. §aStretching Sticks§r)"),
        ENCHANTED_BOOKS("§fUltimate Enchantments\n§r(§d§lFirst Impression I §r& §d§lMissile I §rBooks)"),
        MOBS("§fHuntable Mobs\n§r(Phantoms)"),
        BOOSTERS("§fBoosters\n§r(Sweep and Foraging Wisdom)"),
        SHARDS("§fDirect Shard Drops\n§r(§9Hummingbirds§r and §6Chameleons§r)"),
        RUNES("§fRunes\n§r(§fFading White §rand §aFading Green§r)"),
        MISC("§cTree The Fish"),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(name = "Only Holding Axe", desc = "Only show the tracker while holding an axe.")
    @ConfigEditorBoolean
    var onlyHoldingAxe: Boolean = true

    @Expose
    @ConfigOption(
        name = "Disappearing Delay",
        desc = "The delay in seconds before the tracker disappears after you stop holding an axe."
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 60f, minStep = 1f)
    var disappearingDelay: Int = 15

    @Expose
    @ConfigOption(name = "Show Whole Trees", desc = "Estimate how many full trees you have chopped down, using percentage summing.")
    @ConfigEditorBoolean
    var showWholeTrees: Boolean = true

    @Expose
    @ConfigOption(
        name = "Tracker Settings",
        desc = ""
    )
    @Accordion
    val perTrackerConfig: IndividualItemTrackerConfig = IndividualItemTrackerConfig()
}
