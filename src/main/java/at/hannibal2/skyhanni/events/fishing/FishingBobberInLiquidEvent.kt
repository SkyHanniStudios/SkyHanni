package at.hannibal2.skyhanni.events.fishing

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import net.minecraft.world.entity.projectile.FishingHook

@PrimaryFunction("onBobberInLiquid")
class FishingBobberInLiquidEvent(val bobber: FishingHook, val onWater: Boolean) : SkyHanniEvent()
