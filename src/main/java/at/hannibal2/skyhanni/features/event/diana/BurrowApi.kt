package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.vector.SkyHanniVector

object BurrowApi {

    var lastBurrowRelatedChatMessage = SimpleTimeMark.farPast()
    var lastBurrowInteracted: SkyHanniVector? = null
        private set

    fun setBurrowInteracted(interacted: SkyHanniVector?) {
        if (interacted != null) {
            GriffinBurrowHelper.addDebug("set last interacted burrow to [${interacted.x}, ${interacted.y}, ${interacted.z}]")
        } else GriffinBurrowHelper.addDebug("set last interacted burrow to null")
        lastBurrowInteracted = interacted
    }
}
