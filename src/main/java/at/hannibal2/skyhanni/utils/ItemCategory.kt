@file:Suppress("AnnotationOnSameLine", "AnnotationOnSeparateLine")

package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull

/**
 * Deprecated entries should not be removed, they are kept to avoid errors with unupdated items.
 */
enum class ItemCategory {
    // Weapons
    SWORD,
    LONGSWORD,
    BOW,
    SHORT_BOW,
    WAND,
    FISHING_ROD,
    @Legacy("No longer exists")
    FISHING_WEAPON,
    AXE,
    GAUNTLET,
    DEPLOYABLE,
    VACUUM,
    LASSO,

    // Tools
    ROD_PART,
    PICKAXE,
    DRILL,
    SHOVEL,
    SHEARS,
    @Legacy("No longer exists", ReplaceWith("ItemCategory.FARMING_TOOL"))
    HOE,
    FARMING_TOOL,
    CHISEL,
    WATERING_CAN,
    FISHING_NET,
    TRAP,

    // Armor
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,

    // Equipment
    BELT,
    NECKLACE,
    CLOAK,
    GLOVES,
    BRACELET,

    // Accessories
    ACCESSORY,
    CARNIVAL_MASK,
    POWER_STONE,
    HATCESSORY,
    REFORGE_STONE,
    DYE,

    // Consumables
    BAIT,
    ARROW,
    ARROW_POISON,
    PET_ITEM,
    ENCHANTED_BOOK,
    POTION,
    TRAVEL_SCROLL,
    ABILITY_SCROLL,
    BOOSTER,
    CAPSULE,
    RIFT_TIMECHARM,
    SALT,
    WATER_SHARD,
    FOREST_SHARD,
    COMBAT_SHARD,
    GARDEN_CHIP,
    MUTATION,

    // Creatures
    PET,
    RABBIT,
    SEA_CREATURE,
    TROPHY_FISH,
    TROPHY,

    // Resources
    ORE,
    BLOCK,
    DWARVEN_METAL,
    GEMSTONE,

    // Collectibles
    MEMENTO,
    COSMETIC,
    PORTAL,

    // Other
    SACK,
    ABIPHONE,
    // TODO This was previously used as a fake category for uncategorized dungeon items.
    //  Remove it after ensuring it doesn't break anything.
    @Deprecated("Legacy fake category", ReplaceWith("ItemCategory.NONE"))
    ITEM,

    NONE,
    ;

    fun isDeprecated(): Boolean = this.javaClass.getField(name).getAnnotation(Deprecated::class.java) != null

    companion object {

        fun Collection<ItemCategory>.containsItem(stack: SafeItemStack?) =
            stack?.getItemCategoryOrNull()?.let { this.contains(it) } ?: false

        val miningTools = listOf(PICKAXE, DRILL, GAUNTLET)

        val armor = setOf(HELMET, CHESTPLATE, LEGGINGS, BOOTS)

        val equipment = setOf(NECKLACE, BELT, CLOAK, GLOVES, BRACELET)

        val armorAndEquipment: Set<ItemCategory> = armor + equipment

        val armorAndEquipmentAndMasks: Set<ItemCategory> = armorAndEquipment + CARNIVAL_MASK
    }
}
