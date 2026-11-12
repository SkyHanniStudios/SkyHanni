package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.vector.SkyHanniVector

object LootshareUtils {

    const val RANGE = 30.0f

    data class Sphere(
        val position: SkyHanniVector,
        var color: LorenzColor = LorenzColor.WHITE,
    )

    fun isInRange(pos: SkyHanniVector): Boolean = pos.distanceToPlayer() < RANGE
}
