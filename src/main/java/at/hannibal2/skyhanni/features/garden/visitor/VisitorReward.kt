package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig
import at.hannibal2.skyhanni.data.jsonobjects.repo.GardenJson
import at.hannibal2.skyhanni.data.jsonobjects.repo.GardenVisitorReward
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EnumUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import at.hannibal2.skyhanni.utils.SafeItemStack

private typealias StatsEntry = DropsStatisticsConfig.DropsStatisticsTextEntry

enum class VisitorReward(val displayName: String) {
    FLOWERING_BOUQUET("§9Flowering Bouquet"),
    OVERGROWN_GRASS("§9Overgrown Grass"),
    GREEN_BANDANA("§9Green Bandana"),
    DEDICATION("§9Dedication IV"),
    MUSIC_RUNE("§9Music Rune"),
    SPACE_HELMET("§cSpace Helmet"),
    CULTIVATING("§9Cultivating I"),
    REPLENISH("§9Replenish I"),
    DELICATE("§9Delicate V"),
    COPPER_DYE("§8Copper Dye"),
    JUNGLE_KEY("§5Jungle Key"),
    FRUIT_BOWL("§9Fruit Bowl"),
    HARVEST_HARBINGER("§9Harvest Harbinger V"),
    HYPERCHARGE_CHIP("§9Hypercharge Chip"),
    QUICKDRAW_CHIP("§9Quickdraw Chip"),
    FARMING_EXP_BOOST_EPIC("§5Farming Exp Boost"),
    UNFULFILLED_JERRYSEED("§aUnfulfilled Jerryseed"),
    VOTER_BADGE("§fVoter's Badge"),
    VOTER_BADGE_VIP("§aVIP Voter's Badge"),
    VOTER_BADGE_ELITE("§9Elite Voter's Badge"),
    VOTER_BADGE_SUPREME("§5Supreme Voter's Badge"),
    DYE_WILD_STRAWBERRY("§dWild Strawberry Dye"),
    VELVET_TOP_HAT("§9Velvet Top Hat"),
    CASHMERE_JACKET("§9Cashmere Jacket"),
    SATIN_TROUSERS("§9Satin Trousers"),
    OXFORD_SHOES("§9Oxford Shoes"),
    CARNIVAL_TICKET("§aCarnival Ticket"),
    VISITORS_GRATITUDE("§fVisitors' Gratitude"),
    FARMING_CONTEST_DISPLAY("§aFarming Contest Display"),
    ASTRONAUT_PERSONALITY("§fAstronaut Minion Skin"),
    FAST_FOOD_BARN_SKIN("§6Fast Food Barn Skin"),
    JELLY_GREENHOUSE_SKIN("§6Jelly Garden Greenhouse Skin"),
    ;

    val itemStack: SafeItemStack get() = (rewardInternalNames[this] ?: NeuInternalName.MISSING_ITEM).getItemStack()
    // TODO use this instead of hard coded item names once moulconfig no longer calls toString before the neu repo gets loaded
//     val displayName by lazy { itemStack.nameWithEnchantment ?: internalName.asString() }

    @SkyHanniModule
    companion object {
        private var rewardInternalNames = mapOf<VisitorReward, NeuInternalName>()
        private var rewardsByInternalName = mapOf<NeuInternalName, VisitorReward>()

        fun getByInternalName(internalName: NeuInternalName) = rewardsByInternalName[internalName]

        @HandleEvent
        private fun onRepoReload(event: RepositoryReloadEvent) {
            loadRepoData(event.getConstant<GardenJson>("Garden").visitorRewards)
        }

        internal fun loadRepoData(rewards: Map<String, GardenVisitorReward>) {
            val knownRewards = rewards.mapNotNull { (name, data) ->
                EnumUtils.enumValueOfOrNull<VisitorReward>(name)?.let { it to data }
            }
            rewardInternalNames = knownRewards.associate { (reward, data) -> reward to data.internalName }
            rewardsByInternalName = buildMap {
                for ((reward, data) in knownRewards) {
                    put(data.internalName, reward)
                    for (alias in data.aliases) put(alias, reward)
                }
            }
        }
    }

    // Todo: Remove this when enum names of this and DropsStatisticsTextEntry are in sync
    // TODO never hide an error entirely
    fun toStatsTextEntryOrNull() = when (this) {
        DEDICATION -> StatsEntry.DEDICATION_IV
        MUSIC_RUNE -> StatsEntry.MUSIC_RUNE_I
        CULTIVATING -> StatsEntry.CULTIVATING_I
        REPLENISH -> StatsEntry.REPLENISH_I
        else -> {
            try {
                StatsEntry.valueOf(name)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    override fun toString() = displayName
}
