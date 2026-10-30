package at.hannibal2.skyhanni.events.garden.visitor

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.garden.visitor.VisitorApi
import at.hannibal2.skyhanni.utils.vector.SkyHanniVector

class VisitorRenderEvent(
    val visitor: VisitorApi.Visitor,
    val location: SkyHanniVector,
    val parent: SkyHanniRenderWorldEvent,
) : SkyHanniEvent()
