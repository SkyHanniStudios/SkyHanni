package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.achievements.Achievement
import at.hannibal2.skyhanni.data.model.SkyblockStat
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.events.SackChangeEvent
import at.hannibal2.skyhanni.events.achievements.AchievementRegistrationEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.entity.ItemAddInInventoryEvent
import at.hannibal2.skyhanni.events.item.ShardGainEvent
import at.hannibal2.skyhanni.features.achievements.AchievementManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.repoItemName
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.chat.TextHelper.asComponent
import at.hannibal2.skyhanni.utils.collection.CollectionUtils.evictOldestEntry
import at.hannibal2.skyhanni.utils.collection.TimeLimitedSet
import at.hannibal2.skyhanni.utils.compat.append
import at.hannibal2.skyhanni.utils.compat.componentBuilder
import at.hannibal2.skyhanni.utils.compat.withColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.ChatFormatting
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ItemAddManager {
    enum class Source(val displayName: String) {
        ITEM_ADD("Picked up in inventory"),
        SACKS("Went into Sacks"),
        SHARD("Went into Hunting Box"),
        COMMAND("Invented via command"),
    }

    private val ARCHFIEND_DICE = "ARCHFIEND_DICE".toInternalName()
    private val HIGH_CLASS_ARCHFIEND_DICE = "HIGH_CLASS_ARCHFIEND_DICE".toInternalName()

    @Suppress("MaxLineLength")
    private val diceRollChatPattern by RepoPattern.pattern(
        "data.itemmanager.diceroll",
        "§eYour §r§(?:5|6High Class )Archfiend Dice §r§erolled a §r§.(?<number>.)§r§e! Bonus: §r§.(?<hearts>.*)${SkyblockStat.HEALTH.hypixelIcon}",
    )

    private var inSackInventory = false
    private var lastSackInventoryLeave = SimpleTimeMark.farPast()

    @HandleEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (event.inventoryName.contains("Sack")) {
            inSackInventory = true
        }
    }

    @HandleEvent
    fun onInventoryClose() {
        if (inSackInventory) {
            inSackInventory = false
            lastSackInventoryLeave = SimpleTimeMark.now()
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onSackChange(event: SackChangeEvent) {

        if (inSackInventory || lastSackInventoryLeave.passedSince() < 10.seconds) return

        for (sackChange in event.sackChanges) {
            val change = sackChange.delta
            val internalName = sackChange.internalName
            if (change > 0) {
                Source.SACKS.addItemExceptSuperCrafted(internalName, change)
            }
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onItemAdd(event: ItemAddInInventoryEvent) {

        val internalName = event.internalName
        if (internalName == ARCHFIEND_DICE || internalName == HIGH_CLASS_ARCHFIEND_DICE) {
            if (lastDiceRoll.passedSince() < 500.milliseconds) {
                return
            }
        }

        Source.ITEM_ADD.addItemExceptSuperCrafted(internalName, event.amount)
    }

    @HandleEvent
    fun onShardGain(event: ShardGainEvent) {
        if (event.amount < 0) return
        Source.SHARD.addItem(event.shardInternalName, event.amount)
    }

    private fun Source.addItem(internalName: NeuInternalName, amount: Int) {
        ItemAddEvent(internalName, amount, this).post()
    }

    private val recentItems = mutableMapOf<ItemAddEvent, SimpleTimeMark>()

    @HandleEvent
    fun onItemAdd(event: ItemAddEvent) {
        recentItems[event] = SimpleTimeMark.now()
        recentItems.evictOldestEntry(15)
    }

    @HandleEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Recent Item Adds")
        if (recentItems.isEmpty()) return event.addIrrelevant("no items added")

        val text = formattedList().map { it.removeColor() }
        if (recentItems.values.max().passedSince() < 20.seconds) {
            event.addData(text)
        } else {
            event.addIrrelevant(text)
        }
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.registerBrigadier("shdebugrecentitemadds") {
            description = "Shows recent item additions."
            category = CommandCategory.DEVELOPER_DEBUG
            simpleCallback {
                ChatUtils.clickToClipboard("Recent Item adds", formattedList())
            }
        }
    }

    private fun formattedList() = recentItems.map { (itemAddEvent, time) ->
        val itemName = itemAddEvent.internalName.repoItemName
        val amount = itemAddEvent.amount
        val source = itemAddEvent.source.displayName
        val passedSince = time.passedSince().format()
        "§r$itemName §7(§8x$amount§7) §e$source §b$passedSince ago §7(§b$time§7)"
    }

    private var lastDiceRoll = SimpleTimeMark.farPast()

    private class SuperCraftedItem(val internalName: NeuInternalName, val source: Source, var amount: Int)

    private val superCraftedItems = TimeLimitedSet<SuperCraftedItem>(30.seconds)

    private fun Source.addItemExceptSuperCrafted(internalName: NeuInternalName, amount: Int) {
        var remaining = amount
        for (superCrafted in superCraftedItems) {
            if (superCrafted.internalName != internalName || superCrafted.source != this) continue
            val skipped = minOf(superCrafted.amount, remaining)
            superCrafted.amount -= skipped
            remaining -= skipped
            if (superCrafted.amount == 0) superCraftedItems.remove(superCrafted)
            if (remaining == 0) return
        }
        addItem(internalName, remaining)
    }

    private const val DICE_ACHIEVEMENT = "100 dice rolls"

    @HandleEvent
    fun onAchievementRegistration(event: AchievementRegistrationEvent) {
        val achievement = Achievement(
            "Professional Gambler".asComponent(),
            componentBuilder {
                append("Spin 100 dice")
                append(" I doubt you make money from this...") {
                    withColor(ChatFormatting.DARK_GRAY)
                }
            },
            7f,
            false,
            listOf(100),
        )
        event.register(achievement, DICE_ACHIEVEMENT)
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent.Allow) {
        if (diceRollChatPattern.matches(event.message)) {
            lastDiceRoll = SimpleTimeMark.now()
            val achievement = AchievementManager.getAchievement(DICE_ACHIEVEMENT)
            AchievementManager.updateTieredAchievement(DICE_ACHIEVEMENT, achievement.data.progress + 1)
        }
        val (internalName, amount) = ItemUtils.readSuperCraftedItem(event.message) ?: return
        if (!SackApi.sackListInternalNames.contains(internalName.asString())) return
        superCraftedItems.add(SuperCraftedItem(internalName, Source.ITEM_ADD, amount))
        superCraftedItems.add(SuperCraftedItem(internalName, Source.SACKS, amount))
    }
}
