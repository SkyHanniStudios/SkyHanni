package at.hannibal2.skyhanni.features.hunting

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.toColor

@SkyHanniModule
object HideonleafHighlighter {

    private val config get() = SkyHanniMod.feature.hunting.mobHighlight.hideonleaf

    @HandleEvent(onlyOnIsland = IslandType.GALATEA)
    private fun onSkyblockMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        val mob = event.mob.takeIf { it.name == "HideonLeaf " } ?: return
        RenderLivingEntityHelper.setEntityColor(mob.baseEntity, config.color.toColor()) {
            config.enabled && mob.distanceToPlayer() < 20
        }
    }

    @HandleEvent
    private fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(91, "foraging.hideonleafHighlight", "foraging.mobHighlight.hideonleaf")
        event.move(100, "foraging.mobHighlight.hideonleaf", "hunting.mobHighlight.hideonleaf")
    }
}
