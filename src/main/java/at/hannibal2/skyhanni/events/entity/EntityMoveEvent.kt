package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.GenericSkyHanniEvent
import at.hannibal2.skyhanni.events.entity.abstract.SkyHanniLivingEntityEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.utils.LorenzVec
import net.minecraft.world.entity.LivingEntity

/**
 * This event is fired each tick that an entity of the specified type moved.
 * @param entity Entity being tracked in the given move event.
 * @param oldLocation Entity's location in the prior tick to the event being fired.
 * @param newLocation Entity's current location as the event is being fired.
 * @param distance Amount of distance the entity moved between oldLocation and newLocation.
 */
@PrimaryFunction("onEntityMove")
class EntityMoveEvent<T : LivingEntity>(
    override val entity: T,
    val oldLocation: LorenzVec,
    val newLocation: LorenzVec,
    val distance: Double,
) : GenericSkyHanniEvent<T>(entity.javaClass), SkyHanniLivingEntityEvent<T>
