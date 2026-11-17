package at.hannibal2.skyhanni.config.features.chat

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class PowderMiningConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Hide messages while opening chests in the Crystal Hollows.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigOption(
        name = "Powder",
        desc = "Hide §dGemstone §rand §aMithril §rPowder rewards under a certain amount.\n" +
            "§a0§r: §aShow all\n" +
            "§c60000§r: §cHide all"
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 60000f, minStep = 500f)
    var powderThreshold: Int = 1000

    @Expose
    @ConfigOption(
        name = "Essence",
        desc = "Hide §6Gold §rand §bDiamond §rEssence rewards under a certain amount.\n" +
            "§a0§r: §aShow all\n" +
            "§c20§r: §cHide all"
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 20f, minStep = 1f)
    var essenceThreshold: Int = 5

    enum class SimplePowderMiningRewardTypes(val displayName: String) {
        ASCENSION_ROPE("§9Ascension Rope"),
        WISHING_COMPASS("§aWishing Compass"),
        OIL_BARREL("§aOil Barrel"),
        PREHISTORIC_EGG("§fPrehistoric Egg"),
        PICKONIMBUS("§5Pickonimbus 2000"),
        JUNGLE_HEART("§6Jungle Heart"),
        SLUDGE_JUICE("§aSludge Juice"),
        YOGGIE("§aYoggie"),
        ROBOT_PARTS("§9Robot Parts"),
        TREASURITE("§5Treasurite"),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(name = "Common Items", desc = "Hide reward messages for listed items.")
    @ConfigEditorDraggableList
    val simplePowderMiningTypes: MutableList<SimplePowderMiningRewardTypes> = mutableListOf(
        SimplePowderMiningRewardTypes.ASCENSION_ROPE,
        SimplePowderMiningRewardTypes.WISHING_COMPASS,
        SimplePowderMiningRewardTypes.OIL_BARREL,
        SimplePowderMiningRewardTypes.JUNGLE_HEART,
        SimplePowderMiningRewardTypes.SLUDGE_JUICE,
        SimplePowderMiningRewardTypes.YOGGIE,
        SimplePowderMiningRewardTypes.TREASURITE
    )

    @Expose
    @ConfigOption(name = "Goblin Egg", desc = "Hide Goblin Egg rewards that are below a certain rarity.")
    @ConfigEditorDropdown
    var goblinEggs: GoblinEggEntry = GoblinEggEntry.YELLOW_UP

    enum class GoblinEggEntry(val displayName: String) {
        SHOW_ALL("Show all"),
        HIDE_ALL("Hide all"),
        GREEN_UP("Show §aGreen §rand up"),
        YELLOW_UP("Show §eYellow §rand up"),
        RED_UP("Show §cRed §rand up"),
        BLUE_ONLY("Show §3Blue §ronly"),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(name = "Gemstones", desc = "")
    @Accordion
    val gemstone: PowderMiningGemstoneConfig = PowderMiningGemstoneConfig()
}
