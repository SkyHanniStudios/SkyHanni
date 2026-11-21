package utils

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

object SkyHanniFqNames {
    val ROOT_PACKAGE = FqName("at.hannibal2.skyhanni")
    val CONFIG_PACKAGE = FqName("at.hannibal2.skyhanni.config")
    val CONFIG_FEATURES_PACKAGE = FqName("at.hannibal2.skyhanni.config.features")
    val CONFIG_STORAGE_PACKAGE = FqName("at.hannibal2.skyhanni.config.storage")

    val ISLAND_TYPE: ClassId = classId("at.hannibal2.skyhanni.data.IslandType")
    val SAFE_ITEM_STACK: ClassId = classId("at.hannibal2.skyhanni.utils.SafeItemStack")
    val VANILLA_ITEM_STACK: ClassId = classId("net.minecraft.world.item.ItemStack")
    val HANDLE_EVENT: ClassId = classId("at.hannibal2.skyhanni.api.event.HandleEvent")
    val PRIMARY_FUNCTION: ClassId = classId("at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction")
    val SKYHANNI_MODULE: ClassId = classId("at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule")
    val SKYHANNI_EVENT: ClassId = classId("at.hannibal2.skyhanni.api.event.SkyHanniEvent")

    private fun classId(fqName: String): ClassId = ClassId.topLevel(FqName(fqName))
}
