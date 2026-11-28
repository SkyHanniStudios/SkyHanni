package at.hannibal2.skyhanni.features.event.stockexchange

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.title.TitleManager
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.TimeUtils.timerColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.container.VerticalContainerRenderable.Companion.vertical
import at.hannibal2.skyhanni.utils.renderables.primitives.text
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object StonksAuctionTimer {

    private val config get() = SkyHanniMod.feature.event.stonksAuction
    private val storage get() = ProfileStorageData.profileSpecific?.stonksAuction

    private val patternGroup = RepoPattern.group("event.stonksauction")

    /**
     * REGEX-TEST: Successfully placed your Stonk bid of 2,000,002 Coins!
     */
    private val bidPlacedPattern by patternGroup.pattern(
        "bid.placed",
        "Successfully placed your Stonk bid of (?<amount>[\\d,]+) Coins!",
    )

    /**
     * REGEX-TEST: Successfully increased your Stonk bid by 200,000 Coins!
     */
    private val bidIncreasedPattern by patternGroup.pattern(
        "bid.increased",
        "Successfully increased your Stonk bid by (?<amount>[\\d,]+) Coins!",
    )

    // real rounds are at least 1h40m apart; anything smaller is just re-parse jitter from re-reading the same round's menu item
    private val ROUND_CHANGE_TOLERANCE = 5.minutes

    private var lastSeenRoundEnd = SimpleTimeMark.farPast()
    private var lastWarnedRoundEnd = SimpleTimeMark.farPast()
    private var display: Renderable? = null

    // the value read from the menu until it expires, then the calendar estimate
    private fun roundEnd(storage: ProfileSpecificStorage.StonksAuctionStorage): SimpleTimeMark {
        val exact = storage.roundEnd
        if (exact.isInFuture()) return exact
        return calendarEstimatedRoundEnd()
    }

    private fun hasBidThisRound(storage: ProfileSpecificStorage.StonksAuctionStorage, roundEnd: SimpleTimeMark): Boolean =
        isSameRound(storage.lastBidRoundEnd, roundEnd)

    private fun isSameRound(a: SimpleTimeMark, b: SimpleTimeMark) = a.absoluteDifference(b) <= ROUND_CHANGE_TOLERANCE

    // assumes rounds end on SkyBlock month boundaries, one SkyBlock month is 10h20m
    private fun calendarEstimatedRoundEnd(): SimpleTimeMark {
        val now = SkyBlockTime.now()
        val monthStart = SkyBlockTime(year = now.year, month = now.month)
        return (monthStart + SkyBlockTime.SKYBLOCK_MONTH_MILLIS.milliseconds).toTimeMark()
    }

    // backup for the menu lore; matches the full chat line, so player messages (prefixed with their name) can't trigger it
x    // TODO use SystemMessageEvent once SkyHanniChatEvent and party chat stop extending it, it fires twice and lets party chat through
    @HandleEvent(onlyOnSkyblock = true)
    private fun onChat(event: SkyHanniChatEvent.Allow) {
        val message = event.cleanMessage
        bidPlacedPattern.matchMatcher(message) {
            val storage = storage ?: return
            storage.lastBidRoundEnd = roundEnd(storage)
            storage.bidAmount = group("amount").formatLong()
        }
        bidIncreasedPattern.matchMatcher(message) {
            val storage = storage ?: return
            // the message only contains the increase, so the new total needs the earlier bid from this round
            if (!hasBidThisRound(storage, roundEnd(storage))) return
            storage.bidAmount += group("amount").formatLong()
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    private fun onGuiRenderOverlay() {
        if (!config.countdown) return
        display?.let { config.countdownPosition.renderRenderable(it, posLabel = "Stonks Auction Timer") }
    }

    @HandleEvent(onlyOnSkyblock = true)
    private fun onSecondPassed() {
        if (!Perk.STOCK_EXCHANGE.isActive) {
            display = null
            return
        }
        val storage = storage ?: return
        val roundEnd = roundEnd(storage)
        if (!roundEnd.isInFuture()) {
            display = null
            return
        }

        updateNewRoundState(roundEnd)
        updateOneMinuteWarning(storage, roundEnd)

        display = if (config.countdown) buildDisplay(storage, roundEnd) else null
    }

    private fun buildDisplay(storage: ProfileSpecificStorage.StonksAuctionStorage, roundEnd: SimpleTimeMark): Renderable {
        val timeUntil = roundEnd.timeUntil()
        val color = timeUntil.timerColor("§e")
        val bidStatus = if (hasBidThisRound(storage, roundEnd)) "§a✔ Bid: §e${storage.bidAmount.addSeparators()} Coins" else "§c✘ Not bid!"
        val countdownLine = Renderable.text("§6Stonks Auction §7ends in $color${timeUntil.format(maxUnits = 2)}")
        val bidLine = Renderable.text(bidStatus)
        return Renderable.vertical(countdownLine, bidLine, horizontalAlign = HorizontalAlignment.CENTER)
    }

    private fun updateNewRoundState(roundEnd: SimpleTimeMark) {
        if (isSameRound(roundEnd, lastSeenRoundEnd)) return
        val isFirstObservation = lastSeenRoundEnd.isFarPast()
        lastSeenRoundEnd = roundEnd
        lastWarnedRoundEnd = SimpleTimeMark.farPast()
        if (config.newRoundAlert && !isFirstObservation) {
            ChatUtils.chat("§6A new Stonks Auction round has started! §7Ends in §e${roundEnd.timeUntil().format(maxUnits = 2)}")
        }
    }

    private fun updateOneMinuteWarning(storage: ProfileSpecificStorage.StonksAuctionStorage, roundEnd: SimpleTimeMark) {
        if (!config.oneMinuteWarning) return
        if (roundEnd.timeUntil() > 1.minutes) return
        if (isSameRound(roundEnd, lastWarnedRoundEnd)) return

        lastWarnedRoundEnd = roundEnd
        val bidWarning = if (hasBidThisRound(storage, roundEnd)) "" else " §c§lYou haven't bid yet!"
        val sound = SoundUtils.createSound("block.note_block.pling", pitch = 0.5f, isWarning = true)
        SoundUtils.repeatSound(delay = 100, repeat = 2, sound)
        TitleManager.sendTitle("§6Stonks Auction ending soon!$bidWarning")
        ChatUtils.notifyOrDisable("§6The Stonks Auction round ends in less than a minute!$bidWarning", config::oneMinuteWarning)
    }
}
