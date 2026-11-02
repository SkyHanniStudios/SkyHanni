package at.hannibal2.skyhanni.config

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.hypixel.HypixelJoinEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import com.google.gson.JsonPrimitive

@SkyHanniModule
object UpdateKeybinds {

    private val unknownKeybinds = mutableSetOf<String>()

    @HandleEvent
    private fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        if (event.oldVersion >= 150) {
            return
        }
        for (keybindPath in allKeybindConfigs) {
            event.transform(150, keybindPath) { element ->
                val oldCode = runCatching { element.asInt }.getOrElse {
                    unknownKeybinds.add(keybindPath)
                    -1
                }

                val newStringName = glfwKeyName(oldCode) ?: run {
                    unknownKeybinds.add(keybindPath)
                    glfwKeyName(-1)
                }

                JsonPrimitive(newStringName)
            }
        }
    }

    @HandleEvent
    private fun onHypixelJoin(event: HypixelJoinEvent) {
        if (unknownKeybinds.isEmpty()) return
        ChatUtils.hoverableChat(
            "Some Invalid Keybinds were reset due to a migration from an older version of SkyHanni.\n" +
                "Please check your keybinds in the config and reconfigure them if necessary.",
            hover = unknownKeybinds.toList(),
            prefixColor = "§c",
        )
        unknownKeybinds.clear()
    }

    // Must be hardcoded list to still work with future config migrations
    private val allKeybindConfigs = listOf(
        "config.gui.keyBindOpen",
        "config.gui.keyBindReset",
        "config.garden.visitors.rewardWarning.bypassKey",
        "config.garden.visitors.acceptHotkey",
        "config.garden.keyBind.attack",
        "config.garden.keyBind.useItem",
        "config.garden.keyBind.left",
        "config.garden.keyBind.right",
        "config.garden.keyBind.forward",
        "config.garden.keyBind.back",
        "config.garden.keyBind.jump",
        "config.garden.keyBind.sneak",
        "config.garden.pests.pestFinder.teleportHotkey",
        "config.garden.pests.pestTrap.releaseHotkey",
        "config.garden.tooltipTweak.fortuneTooltipKeybind",
        "config.garden.mouseSensitivityReducer.keybind",
        "config.garden.gardenCommands.homeHotkey",
        "config.garden.gardenCommands.sethomeHotkey",
        "config.garden.gardenCommands.barnHotkey",
        "config.garden.seeThroughWindow.keybind",
        "config.garden.plotBorderKey",
        "config.crimsonIsle.reputationHelper.hotkey",
        "config.crimsonIsle.vanquisherShare.keybindSharing",
        "config.fishing.trophyFishing.display.keybind",
        "config.mining.glaciteMineshaft.waypointsConfig.shareCorpseKeybind",
        "config.mining.tunnelMaps.campfireKey",
        "config.mining.tunnelMaps.nextSpotHotkey",
        "config.hunting.shardTracker.selectShardKeybind",
        "config.hunting.fusionKeybinds.repeatFusionKeybind",
        "config.hunting.fusionKeybinds.confirmFusionKeybind",
        "config.hunting.nextShulkerKeybind",
        "config.combat.instanceChestProfit.keybind",
        "config.dungeon.spiritLeapOverlay.spiritLeapKeybindConfig.keybindOption1",
        "config.dungeon.spiritLeapOverlay.spiritLeapKeybindConfig.keybindOption2",
        "config.dungeon.spiritLeapOverlay.spiritLeapKeybindConfig.keybindOption3",
        "config.dungeon.spiritLeapOverlay.spiritLeapKeybindConfig.keybindOption4",
        "config.inventory.auctions.copyUnderbidKeybind",
        "config.inventory.helper.harp.harpKeybinds.key1",
        "config.inventory.helper.harp.harpKeybinds.key2",
        "config.inventory.helper.harp.harpKeybinds.key3",
        "config.inventory.helper.harp.harpKeybinds.key4",
        "config.inventory.helper.harp.harpKeybinds.key5",
        "config.inventory.helper.harp.harpKeybinds.key6",
        "config.inventory.helper.harp.harpKeybinds.key7",
        "config.inventory.customWardrobe.tooltipKeybind",
        "config.inventory.customWardrobe.keybinds.slot1",
        "config.inventory.customWardrobe.keybinds.slot2",
        "config.inventory.customWardrobe.keybinds.slot3",
        "config.inventory.customWardrobe.keybinds.slot4",
        "config.inventory.customWardrobe.keybinds.slot5",
        "config.inventory.customWardrobe.keybinds.slot6",
        "config.inventory.customWardrobe.keybinds.slot7",
        "config.inventory.customWardrobe.keybinds.slot8",
        "config.inventory.customWardrobe.keybinds.slot9",
        "config.inventory.customLoadout.keybinds.slot1",
        "config.inventory.customLoadout.keybinds.slot2",
        "config.inventory.customLoadout.keybinds.slot3",
        "config.inventory.customLoadout.keybinds.slot4",
        "config.inventory.customLoadout.keybinds.slot5",
        "config.inventory.customLoadout.keybinds.slot6",
        "config.inventory.customLoadout.keybinds.slot7",
        "config.inventory.customLoadout.keybinds.slot8",
        "config.inventory.customLoadout.keybinds.slot9",
        "config.inventory.customLoadout.keybinds.slot10",
        "config.inventory.customLoadout.keybinds.slot11",
        "config.inventory.customLoadout.keybinds.slot12",
        "config.inventory.chocolateFactory.keybinds.key1",
        "config.inventory.chocolateFactory.keybinds.key2",
        "config.inventory.chocolateFactory.keybinds.key3",
        "config.inventory.chocolateFactory.keybinds.key4",
        "config.inventory.chocolateFactory.keybinds.key5",
        "config.inventory.chocolateFactory.keybinds.key6",
        "config.inventory.chocolateFactory.keybinds.key7",
        "config.inventory.personalCompactor.keybind",
        "config.inventory.focusMode.toggleKey",
        "config.inventory.jacobFarmingContests.openOnElite",
        "config.inventory.estimatedItemValues.hotkey",
        "config.inventory.gfs.keybind",
        "config.inventory.gfs.compactorKeybind",
        "config.inventory.pageScrolling.bypassKey",
        "config.event.diana.keyBindWarp",
        "config.event.diana.rareMobsSharing.keyBindShare",
        "config.event.hoppityEggs.eventSummary.liveDisplay.toggleKeybind",
        "config.chat.peekChat",
        "config.misc.commands.betterWiki.wikiKeybind",
        "config.misc.trevorTheTrapper.keyBind",
        "config.misc.abiphoneAcceptKey",
        "config.dev.debug.copyInternalName",
        "config.dev.debug.copyCosmeticsSkullData",
        "config.dev.debug.copyItemData",
        "config.dev.debug.copyItemDataCompressed",
        "config.dev.debug.bypassAdvancedPlayerTabList",
        "config.dev.debug.trackSound.toggleKeybind",
        "config.dev.debug.trackParticle.toggleKeybind",
        "config.dev.showSlotNumberKey",
        "config.dev.waypoint.saveKey",
        "config.dev.waypoint.deleteKey",
        "config.dev.devTool.graph.placeKey",
        "config.dev.devTool.graph.selectKey",
        "config.dev.devTool.graph.selectRaycastKey",
        "config.dev.devTool.graph.connectKey",
        "config.dev.devTool.graph.exitKey",
        "config.dev.devTool.graph.editKey",
        "config.dev.devTool.graph.textKey",
        "config.dev.devTool.graph.dijkstraKey",
        "config.dev.devTool.graph.saveKey",
        "config.dev.devTool.graph.loadKey",
        "config.dev.devTool.graph.clearKey",
        "config.dev.devTool.graph.throughBlocksKey",
        "config.dev.devTool.graph.tutorialKey",
        "config.dev.devTool.graph.splitKey",
        "config.dev.devTool.graph.dissolveKey",
        "config.dev.devTool.graph.edgeCycle",
    )

    // https://github.com/Polyfrost/OneConfig/blob/feat/26.3/minecraft/src/main/java/org/polyfrost/oneconfig/api/ui/v1/keybind/internal/MinecraftKeybindCodec.java
    // Note that "key.keyboard.application" and "key.keyboard.keypad.period" are 26.3 only.
    // and as such got special handling in SkyhanniTypeAdapter.
    @Suppress("CyclomaticComplexMethod")
    private fun glfwKeyName(code: Int): String? {
        if (code == -1) return "key.keyboard.unknown"
        if (code in 3..7) return "key.mouse." + (code + 1)
        if (code in 48..57) return "key.keyboard." + code.toChar()
        if (code in 65..90) return "key.keyboard." + (code + 32).toChar()
        if (code in 290..314) return "key.keyboard.f" + (code - 289)
        if (code in 320..329) return "key.keyboard.keypad." + (code - 320)
        return when (code) {
            0 -> "key.mouse.left"
            1 -> "key.mouse.right"
            2 -> "key.mouse.middle"
            32 -> "key.keyboard.space"
            39 -> "key.keyboard.apostrophe"
            44 -> "key.keyboard.comma"
            45 -> "key.keyboard.minus"
            46 -> "key.keyboard.period"
            47 -> "key.keyboard.slash"
            59 -> "key.keyboard.semicolon"
            61 -> "key.keyboard.equal"
            91 -> "key.keyboard.left.bracket"
            92 -> "key.keyboard.backslash"
            93 -> "key.keyboard.right.bracket"
            96 -> "key.keyboard.grave.accent"
            161 -> "key.keyboard.world.1"
            162 -> "key.keyboard.world.2"
            256 -> "key.keyboard.escape"
            257 -> "key.keyboard.enter"
            258 -> "key.keyboard.tab"
            259 -> "key.keyboard.backspace"
            260 -> "key.keyboard.insert"
            261 -> "key.keyboard.delete"
            262 -> "key.keyboard.right"
            263 -> "key.keyboard.left"
            264 -> "key.keyboard.down"
            265 -> "key.keyboard.up"
            266 -> "key.keyboard.page.up"
            267 -> "key.keyboard.page.down"
            268 -> "key.keyboard.home"
            269 -> "key.keyboard.end"
            280 -> "key.keyboard.caps.lock"
            281 -> "key.keyboard.scroll.lock"
            282 -> "key.keyboard.num.lock"
            283 -> "key.keyboard.print.screen"
            284 -> "key.keyboard.pause"
            330 -> "key.keyboard.keypad.period"
            331 -> "key.keyboard.keypad.divide"
            332 -> "key.keyboard.keypad.multiply"
            333 -> "key.keyboard.keypad.subtract"
            334 -> "key.keyboard.keypad.add"
            335 -> "key.keyboard.keypad.enter"
            336 -> "key.keyboard.keypad.equal"
            340 -> "key.keyboard.left.shift"
            341 -> "key.keyboard.left.control"
            342 -> "key.keyboard.left.alt"
            343 -> "key.keyboard.left.win"
            344 -> "key.keyboard.right.shift"
            345 -> "key.keyboard.right.control"
            346 -> "key.keyboard.right.alt"
            347 -> "key.keyboard.right.win"
            348 -> "key.keyboard.application"
            else -> null
        }
    }
}
