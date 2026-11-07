@file:Suppress("AnnotationOnSameLine", "AnnotationOnSeparateLine")

package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull

/**
 * Deprecated entries should not be removed, they are kept to avoid errors with unupdated items.
 * Please add new Categories in alphabetical order and new entries to the Categories alphabetically.
 */
enum class ItemCategory {

    // Accessories
    ACCESSORY,
    CARNIVAL_MASK,
    HATCESSORY,
    POWER_STONE,

    // Armor
    BOOTS,
    CHESTPLATE,
    HELMET,
    LEGGINGS,

    // Collectibles
    COSMETIC,
    DYE,
    MEMENTO,
    PORTAL,
    RABBIT,
    RIFT_TIMECHARM,

    // Consumables
    ARROW,
    ARROW_POISON,
    BAIT,
    COMBAT_SHARD,
    FOREST_SHARD,
    GARDEN_CHIP,
    POTION,
    TRAVEL_SCROLL,
    WATER_SHARD,

    // Creatures
    PET,
    SEA_CREATURE,

    // Equipment
    BELT,
    BRACELET,
    CLOAK,
    GLOVES,
    NECKLACE,

    // Hunting Tools
    CAPSULE,
    FISHING_NET,
    LASSO,
    SALT,
    TRAP,

    // Other
    ABIPHONE,
    MUTATION,
    SACK,

    // Resources
    BLOCK,
    DWARVEN_METAL,
    GEMSTONE,
    ORE,
    TROPHY,
    TROPHY_FISH,

    // Tools
    CHISEL,
    DRILL,
    FARMING_TOOL,
    PICKAXE,
    SHEARS,
    SHOVEL,
    WATERING_CAN,

    // Upgrades
    ABILITY_SCROLL,
    BOOSTER,
    ENCHANTED_BOOK,
    PET_ITEM,
    REFORGE_STONE,
    ROD_PART,

    // Weapons
    AXE,
    BOW,
    DEPLOYABLE,
    FISHING_ROD,
    GAUNTLET,
    LONGSWORD,
    SHORT_BOW,
    SWORD,
    VACUUM,
    WAND,

    // Legacy
    @Legacy("No longer exists")
    FISHING_WEAPON,
    @Legacy("No longer exists", ReplaceWith("ItemCategory.FARMING_TOOL"))
    HOE,
    // TODO This was previously used as a fake category for uncategorized dungeon items.
    //  Remove it after ensuring it doesn't break anything.
    @Deprecated("Legacy fake category", ReplaceWith("ItemCategory.NONE"))
    ITEM,
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
