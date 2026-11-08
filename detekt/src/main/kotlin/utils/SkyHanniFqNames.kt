package utils

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

object SkyHanniFqNames {
    const val ROOT_PACKAGE = "at.hannibal2.skyhanni"
    const val EXPOSE = "com.google.gson.annotations.Expose"
    const val ISLAND_TYPE = "at.hannibal2.skyhanni.data.IslandType"
    const val MINECRAFT_COMPAT = "at.hannibal2.skyhanni.utils.compat.MinecraftCompat"
    const val SAFE_ITEM_STACK = "at.hannibal2.skyhanni.utils.SafeItemStack"
    const val VANILLA_ITEM_STACK = "net.minecraft.world.item.ItemStack"
    const val HANDLE_EVENT = "at.hannibal2.skyhanni.api.event.HandleEvent"
    const val PRIMARY_FUNCTION = "at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction"
    const val SKYHANNI_MODULE = "at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule"
    const val SKYHANNI_EVENT = "at.hannibal2.skyhanni.api.event.SkyHanniEvent"
    const val REPO_PATTERN = "at.hannibal2.skyhanni.utils.repopatterns.RepoPattern"
    const val REPO_PATTERN_GROUP = "at.hannibal2.skyhanni.utils.repopatterns.RepoPatternGroup"
    const val REPO_PATTERN_LIST = "at.hannibal2.skyhanni.utils.repopatterns.RepoPatternList"

    val HANDLE_EVENT_ID: ClassId = HANDLE_EVENT.toClassId()
    val PRIMARY_FUNCTION_ID: ClassId = PRIMARY_FUNCTION.toClassId()
    val SKYHANNI_MODULE_ID: ClassId = SKYHANNI_MODULE.toClassId()
    val SKYHANNI_EVENT_ID: ClassId = SKYHANNI_EVENT.toClassId()
    val REPO_PATTERN_ID: ClassId = REPO_PATTERN.toClassId()
    val REPO_PATTERN_GROUP_ID: ClassId = REPO_PATTERN_GROUP.toClassId()
    val REPO_PATTERN_LIST_ID: ClassId = REPO_PATTERN_LIST.toClassId()

    private fun String.toClassId(): ClassId = ClassId.topLevel(FqName(this))
}
