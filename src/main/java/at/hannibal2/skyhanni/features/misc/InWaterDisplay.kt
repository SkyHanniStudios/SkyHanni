package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.entity.EntityMoveEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.primitives.text
import net.minecraft.client.player.LocalPlayer

@SkyHanniModule
object InWaterDisplay {

    private val config get() = SkyHanniMod.feature.misc.stranded
    private val inWaterRenderable by lazy { Renderable.text("§7In Water: §aTrue") }
    private val outOfWaterRenderable by lazy { Renderable.text("§7In Water: §cFalse") }

    private var displayRenderable: Renderable? = null

    @HandleEvent(onlyOnSkyblock = true)
    private fun onGuiRenderOverlay() {
        if (!config.inWaterDisplay) return

        val displayRenderable = displayRenderable ?: return
        config.inWaterPosition.renderRenderable(displayRenderable, posLabel = "In Water Display")
    }

    @HandleEvent(onlyOnSkyblock = true)
    private fun onEntityMove(event: EntityMoveEvent<LocalPlayer>) {
        if (!config.inWaterDisplay) return
        displayRenderable = if (MinecraftCompat.localPlayerOrThrow.isInWater) inWaterRenderable
        else outOfWaterRenderable
    }
}
