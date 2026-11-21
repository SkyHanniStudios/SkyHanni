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
        "gui.keyBindOpen",
        "gui.keyBindReset",
        "garden.visitors.rewardWarning.bypassKey",
        "garden.visitors.acceptHotkey",
        "garden.keyBind.attack",
        "garden.keyBind.useItem",
        "garden.keyBind.left",
        "garden.keyBind.right",
        "garden.keyBind.forward",
        "garden.keyBind.back",
        "garden.keyBind.jump",
        "garden.keyBind.sneak",
        "garden.pests.pestFinder.teleportHotkey",
        "garden.pests.pestTrap.releaseHotkey",
        "garden.tooltipTweak.fortuneTooltipKeybind",
        "garden.mouseSensitivityReducer.keybind",
        "garden.gardenCommands.homeHotkey",
        "garden.gardenCommands.sethomeHotkey",
        "garden.gardenCommands.barnHotkey",
        "garden.seeThroughWindow.keybind",
        "garden.plotBorderKey",
        "crimsonIsle.reputationHelper.hotkey",
        "crimsonIsle.vanquisherShare.keybindSharing",
        "fishing.trophyFishing.display.keybind",
        "mining.glaciteMineshaft.waypointsConfig.shareCorpseKeybind",
        "mining.tunnelMaps.campfireKey",
        "mining.tunnelMaps.nextSpotHotkey",
        "hunting.shardTracker.selectShardKeybind",
        "hunting.fusionKeybinds.repeatFusionKeybind",
        "hunting.fusionKeybinds.confirmFusionKeybind",
        "hunting.nextShulkerKeybind",
        "combat.instanceChestProfit.keybind",
        "dungeon.spiritLeapOverlay.spiritLeapKeybindConfig.keybindOption1",
        "dungeon.spiritLeapOverlay.spiritLeapKeybindConfig.keybindOption2",
        "dungeon.spiritLeapOverlay.spiritLeapKeybindConfig.keybindOption3",
        "dungeon.spiritLeapOverlay.spiritLeapKeybindConfig.keybindOption4",
        "inventory.auctions.copyUnderbidKeybind",
        "inventory.helper.harp.harpKeybinds.key1",
        "inventory.helper.harp.harpKeybinds.key2",
        "inventory.helper.harp.harpKeybinds.key3",
        "inventory.helper.harp.harpKeybinds.key4",
        "inventory.helper.harp.harpKeybinds.key5",
        "inventory.helper.harp.harpKeybinds.key6",
        "inventory.helper.harp.harpKeybinds.key7",
        "inventory.customWardrobe.tooltipKeybind",
        "inventory.customWardrobe.keybinds.slot1",
        "inventory.customWardrobe.keybinds.slot2",
        "inventory.customWardrobe.keybinds.slot3",
        "inventory.customWardrobe.keybinds.slot4",
        "inventory.customWardrobe.keybinds.slot5",
        "inventory.customWardrobe.keybinds.slot6",
        "inventory.customWardrobe.keybinds.slot7",
        "inventory.customWardrobe.keybinds.slot8",
        "inventory.customWardrobe.keybinds.slot9",
        "inventory.customLoadout.keybinds.slot1",
        "inventory.customLoadout.keybinds.slot2",
        "inventory.customLoadout.keybinds.slot3",
        "inventory.customLoadout.keybinds.slot4",
        "inventory.customLoadout.keybinds.slot5",
        "inventory.customLoadout.keybinds.slot6",
        "inventory.customLoadout.keybinds.slot7",
        "inventory.customLoadout.keybinds.slot8",
        "inventory.customLoadout.keybinds.slot9",
        "inventory.customLoadout.keybinds.slot10",
        "inventory.customLoadout.keybinds.slot11",
        "inventory.customLoadout.keybinds.slot12",
        "inventory.chocolateFactory.keybinds.key1",
        "inventory.chocolateFactory.keybinds.key2",
        "inventory.chocolateFactory.keybinds.key3",
        "inventory.chocolateFactory.keybinds.key4",
        "inventory.chocolateFactory.keybinds.key5",
        "inventory.chocolateFactory.keybinds.key6",
        "inventory.chocolateFactory.keybinds.key7",
        "inventory.personalCompactor.keybind",
        "inventory.focusMode.toggleKey",
        "inventory.jacobFarmingContests.openOnElite",
        "inventory.estimatedItemValues.hotkey",
        "inventory.gfs.keybind",
        "inventory.gfs.compactorKeybind",
        "inventory.pageScrolling.bypassKey",
        "event.diana.keyBindWarp",
        "event.diana.rareMobsSharing.keyBindShare",
        "event.hoppityEggs.eventSummary.liveDisplay.toggleKeybind",
        "chat.peekChat",
        "misc.commands.betterWiki.wikiKeybind",
        "misc.trevorTheTrapper.keyBind",
        "misc.abiphoneAcceptKey",
        "dev.debug.copyInternalName",
        "dev.debug.copyCosmeticsSkullData",
        "dev.debug.copyItemData",
        "dev.debug.copyItemDataCompressed",
        "dev.debug.bypassAdvancedPlayerTabList",
        "dev.debug.trackSound.toggleKeybind",
        "dev.debug.trackParticle.toggleKeybind",
        "dev.showSlotNumberKey",
        "dev.waypoint.saveKey",
        "dev.waypoint.deleteKey",
        "dev.devTool.graph.placeKey",
        "dev.devTool.graph.selectKey",
        "dev.devTool.graph.selectRaycastKey",
        "dev.devTool.graph.connectKey",
        "dev.devTool.graph.exitKey",
        "dev.devTool.graph.editKey",
        "dev.devTool.graph.textKey",
        "dev.devTool.graph.dijkstraKey",
        "dev.devTool.graph.saveKey",
        "dev.devTool.graph.loadKey",
        "dev.devTool.graph.clearKey",
        "dev.devTool.graph.throughBlocksKey",
        "dev.devTool.graph.tutorialKey",
        "dev.devTool.graph.splitKey",
        "dev.devTool.graph.dissolveKey",
        "dev.devTool.graph.edgeCycle",
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
