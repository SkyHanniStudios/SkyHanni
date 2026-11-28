package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.GenericSkyHanniEvent
import at.hannibal2.skyhanni.events.entity.abstract.SkyHanniLivingEntityEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import at.hannibal2.skyhanni.utils.SafeItemStack
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity

@PrimaryFunction("onEntityEquipmentChange")
data class EntityEquipmentChangeEvent<T : LivingEntity>(
    override val entity: T,
    val equipmentSlot: EquipmentSlot,
    val newItemStack: SafeItemStack?,
) : GenericSkyHanniEvent<T>(entity.javaClass), SkyHanniLivingEntityEvent<T> {

    val isHead get() = equipmentSlot == EquipmentSlot.HEAD
    val isChest get() = equipmentSlot == EquipmentSlot.CHEST
    val isLeggings get() = equipmentSlot == EquipmentSlot.LEGS
    val isFeet get() = equipmentSlot == EquipmentSlot.FEET
    val isHand get() = equipmentSlot == EquipmentSlot.MAINHAND
}
