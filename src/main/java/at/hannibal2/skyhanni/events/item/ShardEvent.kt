package at.hannibal2.skyhanni.events.item

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.NeuInternalName

// TODO disambiguate verb-wise what the difference between these two is.
//  They both currently use "onShardGain" as their primary function.
/**
 * All shard events, including fusions, syphoning, and sending to Hunting Box.
 */
@Suppress("MissingPrimaryFunction")
open class ShardEvent(
    val shardInternalName: NeuInternalName,
    val amount: Int,
    val source: ShardSource,
) : SkyHanniEvent()

/**
 * Shard events that are explicitly the player gaining shards, for profit trackers and such.
 */
@Suppress("MissingPrimaryFunction")
class ShardGainEvent(
    shardInternalName: NeuInternalName,
    amount: Int,
    source: ShardSource,
) : ShardEvent(shardInternalName, amount, source)

enum class ShardSource {
    SYPHON,
    FUSE,
    CHARM,
    HUNT,
    FISHING,
    SENT_TO_HUNTING_BOX,
    CAPTURED,
    FLOOR_DROP,
    GIVEN,
    BOSS,
}
