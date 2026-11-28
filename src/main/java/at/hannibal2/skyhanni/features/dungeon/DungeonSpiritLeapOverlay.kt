package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.minecraft.KeyDownEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SafeItemStack
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat

@SkyHanniModule
object DungeonSpiritLeapOverlay {
    internal val config get() = SkyHanniMod.feature.dungeon.spiritLeapOverlay

    /**
     * REGEX-TEST: Spirit Leap
     * REGEX-TEST: Teleport to Player
     */
    private val inventoryMenuPattern by DungeonApi.patternGroup.pattern(
        "spirit-leap.menu",
        "Spirit Leap|Teleport to Player",
    )

    data class PlayerStackInfo(val playerInfo: DungeonApi.TeamMember?, val stack: SafeItemStack, val slotNumber: Int)

    internal fun shouldReplace(inventoryName: String): Boolean = isEnabled() && inventoryMenuPattern.matches(inventoryName)

    @HandleEvent
    private fun onKeyPress(event: KeyDownEvent) {
        if (!isEnabled() || !config.spiritLeapKeybindConfig.enableKeybind) return
        val screen = MinecraftCompat.screen as? DungeonSpiritLeapScreen ?: return
        screen.leap(spiritLeapKeybinds.indexOf(event.keyCode))
    }

    internal val spiritLeapKeybinds
        get() = intArrayOf(
            config.spiritLeapKeybindConfig.keybindOption1,
            config.spiritLeapKeybindConfig.keybindOption2,
            config.spiritLeapKeybindConfig.keybindOption3,
            config.spiritLeapKeybindConfig.keybindOption4,
        )

    internal fun leapToPlayer(player: PlayerStackInfo) {
        val playerInfo = player.playerInfo ?: return
        if (playerInfo.playerDead) {
            ChatUtils.chat("§cCannot leap - §e${playerInfo.username} §cis dead.")
            return
        }
        InventoryUtils.clickSlot(player.slotNumber, button = MIDDLE_CLICK)
    }

    private fun isEnabled() = config.enabled && DungeonApi.inDungeon() && DungeonApi.started && !DungeonApi.completed

    @HandleEvent
    private fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(98, "dungeon.spiritLeapOverlay.archerClassColor", "dungeon.spiritLeapOverlay.colorConfig.archerClassColor")
        event.move(98, "dungeon.spiritLeapOverlay.mageClassColor", "dungeon.spiritLeapOverlay.colorConfig.mageClassColor")
        event.move(98, "dungeon.spiritLeapOverlay.berserkClassColor", "dungeon.spiritLeapOverlay.colorConfig.berserkClassColor")
        event.move(98, "dungeon.spiritLeapOverlay.tankClassColor", "dungeon.spiritLeapOverlay.colorConfig.tankClassColor")
        event.move(98, "dungeon.spiritLeapOverlay.healerClassColor", "dungeon.spiritLeapOverlay.colorConfig.healerClassColor")
        event.move(98, "dungeon.spiritLeapOverlay.defaultColor", "dungeon.spiritLeapOverlay.colorConfig.defaultColor")
        event.move(98, "dungeon.spiritLeapOverlay.deadTeammateColor", "dungeon.spiritLeapOverlay.colorConfig.deadTeammateColor")
    }
}
