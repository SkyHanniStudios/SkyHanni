package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import net.minecraft.world.item.Item
import kotlin.time.Duration

object ItemBlink {

    private val offsets = mutableMapOf<Item, Long>()
    private var lastOffset = 0L
    private var endOfBlink = SimpleTimeMark.farPast()
    private var blinkItem: SafeItemStack? = null

    fun setBlink(item: SafeItemStack?, duration: Duration) {
        endOfBlink = duration.fromNow()
        blinkItem = item
    }

    fun SafeItemStack.checkBlinkItem(): SafeItemStack {
        val stack = blinkItem ?: return this
        if (endOfBlink.isInPast()) return this

        val offset: Long = offsets.getOrPut(itemType) {
            lastOffset += 200
            val number = lastOffset % 1000
            offsets[itemType] = number
            number
        }

        return if ((offset + SimpleTimeMark.now().toMillis()) % 1000 > 500) stack else this
    }
}
