package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.data.SlayerApi
import at.hannibal2.skyhanni.data.jsonobjects.repo.SlayerProfitTrackerItemsJson
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.events.PurseChangeCause
import at.hannibal2.skyhanni.events.PurseChangeEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.slayer.SlayerChangeEvent
import at.hannibal2.skyhanni.features.misc.ReplaceRomanNumerals
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.NumberUtil.formatPercentage
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderDisplayConfig
import at.hannibal2.skyhanni.utils.SkyBlockUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.collection.RenderableCollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.SessionUptime
import at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker
import at.hannibal2.skyhanni.utils.tracker.data.ItemTrackerData
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.annotations.Expose

@SkyHanniModule
object SlayerProfitTracker : SkyHanniItemTracker<SlayerProfitTracker.Data>("Slayer Profit Tracker") {
    override val config get() = SlayerApi.config.itemProfitTracker
    override val storageAccessor: (ProfileSpecificStorage) -> Data = {
        it.slayerProfitData.getOrPut(category) { Data() }
    }
    override val renderConfig = RenderDisplayConfig(
        outsideInventory = true,
        inOwnInventory = true,
        condition = { shouldShowDisplay() },
    )
    override val name: String
        get() = "$categoryName Profit Tracker"

    private var category = ""
    private val categoryName get() = ReplaceRomanNumerals.replaceLine(category)
    private var baseSlayerType = ""

    /**
     * REGEX-TEST: Took 1.9k coins from your bank for auto-slayer...
     */
    private val autoSlayerBankPattern by RepoPattern.pattern(
        "slayer.autoslayer.bank.chat.colorless",
        "Took (?<coins>.+) coins from your bank for auto-slayer\\.\\.\\.",
    )

    data class Data(
        @Expose var slayerSpawnCost: Long = 0L,
        @Expose var slayerCompletedCount: Long = 0L,
    ) : ItemTrackerData<SessionUptime.Normal>() {
        override fun getDescription(timesGained: Long): List<String> {
            val percentage = timesGained.toDouble() / slayerCompletedCount
            val perBoss = percentage.coerceAtMost(1.0).formatPercentage()

            return listOf(
                "§7Dropped §e${timesGained.addSeparators()} §7times.",
                "§7Your drop rate: §c$perBoss",
            )
        }

        override fun getCoinName(item: TrackedItem) = "§6Mob Kill Coins"

        override fun getCoinDescription(item: TrackedItem): List<String> {
            val mobKillCoinsFormat = item.totalAmount.shortFormat()
            return listOf(
                "§7Killing mobs gives you coins (more with scavenger).",
                "§7You got §6$mobKillCoinsFormat coins §7that way.",
            )
        }
    }

    private fun addSlayerCosts(price: Double) {
        require(price < 0) { "slayer costs can not be positive" }
        if (category.isEmpty()) return
        modify {
            it.slayerSpawnCost += price.toInt()
        }
    }

    private var allowedItems = mapOf<String, List<NeuInternalName>>()

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        allowedItems = event.getConstant<SlayerProfitTrackerItemsJson>("SlayerProfitTrackerItems").slayers
    }

    @HandleEvent
    fun onPurseChange(event: PurseChangeEvent) {
        if (!isEnabled()) return
        val coins = event.coins
        if (event.reason == PurseChangeCause.GAIN_MOB_KILL && SlayerApi.isInCorrectArea) {
            if (coins >= 100000) {
                ChatUtils.debug("Mob kill coin gain too high! Ignoring!")
                return
            }
            tryAddItem(NeuInternalName.SKYBLOCK_COIN, coins.toInt(), command = false)
        }
        // TODO spawn costs in repo
        if (event.reason == PurseChangeCause.LOSE_SLAYER_QUEST_STARTED) {
            val coinsCap = -350_000
            if (coins < coinsCap || coins > 0) {
                ChatUtils.debug("Wrong Slayer Spawn Cost! Ignoring!")
                return
            }
            addSlayerCosts(coins)
        }
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent.Allow) {
        if (!isEnabled()) return
        autoSlayerBankPattern.matchMatcher(event.cleanMessage) {
            addSlayerCosts(-group("coins").formatDouble())
        }
    }

    @HandleEvent
    fun onSlayerChange(event: SlayerChangeEvent) {
        val newSlayer = event.newSlayer
        category = newSlayer.removeColor()
        baseSlayerType = category.substringBeforeLast(" ")
        if (category.isNotEmpty()) update()
    }

    @HandleEvent
    fun onSlayerQuestComplete() {
        if (category.isEmpty()) return
        modify {
            it.slayerCompletedCount++
        }
    }

    @HandleEvent
    fun onItemAdd(event: ItemAddEvent) {
        // TODO remove is config enabled check for tracker
        if (!isEnabled()) return
        if (!SlayerApi.isInCorrectArea) return
        if (!SlayerApi.hasActiveQuest()) return

        tryAddItem(event.internalName, event.amount, event.source == ItemAddManager.Source.COMMAND)
    }

    private fun tryAddItem(internalName: NeuInternalName, amount: Int, command: Boolean) {
        if (!isAllowedItem(internalName) && internalName != NeuInternalName.SKYBLOCK_COIN) {
            ChatUtils.debug("Ignored non-slayer item pickup: '$internalName' '$category'")
            return
        }

        if (category.isEmpty()) return
        addItem(internalName, amount, command)
    }

    private fun isAllowedItem(internalName: NeuInternalName): Boolean {
        val allowedList = allowedItems[baseSlayerType] ?: return false
        return internalName in allowedList
    }

    override fun drawDisplayF(data: Data) = buildList {
        if (category.isEmpty()) return@buildList
        addSearchString("§e§l$categoryName Profit Tracker")

        var profit = drawItems(data, { true }, this)
        val slayerSpawnCost = data.slayerSpawnCost
        val slayerSpawnCostFormat = slayerSpawnCost.shortFormat()
        add(
            Renderable.hoverTips(
                " §7Slayer Spawn Costs: §c$slayerSpawnCostFormat",
                listOf("§7You paid §c$slayerSpawnCostFormat §7in total", "§7for starting the slayer quests."),
            ).toSearchable(),
        )
        profit += slayerSpawnCost

        val slayerCompletedCount = data.slayerCompletedCount.addSeparators()
        add(
            Renderable.hoverTips(
                "§7Bosses killed: §e$slayerCompletedCount",
                listOf(
                    "§7You killed the $categoryName boss",
                    "§e$slayerCompletedCount §7times.",
                ),
            ).toSearchable(),
        )

        val duration = data.getTotalUptime()
        addAll(addTotalProfit(profit, data.slayerCompletedCount, "boss", duration, "Bosses"))

        addPriceFromButton(this)
    }

    private fun shouldShowDisplay(): Boolean = isEnabled() && SlayerApi.isInCorrectArea

    fun isEnabled() = SkyBlockUtils.inSkyBlock && config.enabled

    override fun resetCommand() = if (category == "") ChatUtils.userError(
        "No current slayer data found! §eGo to a slayer area and start the specific slayer type you want to reset the data of.",
    ) else super.resetCommand()

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.registerBrigadier("shresetslayerprofits") {
            description = "Resets the total slayer profit for the current slayer type"
            category = CommandCategory.USERS_RESET
            simpleCallback { resetCommand() }
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(10, "#profile.slayerProfitData") { old ->
            for (data in old.asJsonObject.entrySet().map { it.value.asJsonObject }) {
                val items = data.get("items").asJsonObject
                for (item in items.entrySet().map { it.value.asJsonObject }) {
                    val oldValue = item.get("timesDropped")
                    item.add("timesGained", oldValue)
                }

                val coinAmount = data.get("mobKillCoins")
                val coins = JsonObject()
                coins.add("internalName", JsonPrimitive("SKYBLOCK_COIN"))
                coins.add("timesDropped", JsonPrimitive(1))
                coins.add("totalAmount", coinAmount)
                items.add("SKYBLOCK_COIN", coins)
            }

            old
        }
        val base = "slayer.itemProfitTracker"
        event.move(150, "$base.pos", "$base.position")
    }
}
