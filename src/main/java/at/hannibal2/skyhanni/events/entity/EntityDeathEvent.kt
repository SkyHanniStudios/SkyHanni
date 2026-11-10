package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.GenericSkyHanniEvent
import net.minecraft.world.entity.Entity

// TODO, I'm redoing all of these already. -Dae
@Suppress("MissingPrimaryFunction")
class EntityDeathEvent<T : Entity>(val entity: T) : GenericSkyHanniEvent<T>(entity.javaClass)
