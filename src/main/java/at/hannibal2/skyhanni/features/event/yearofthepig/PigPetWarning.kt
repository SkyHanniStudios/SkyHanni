package at.hannibal2.skyhanni.features.event.yearofthepig

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.pet.CurrentPetApi
import at.hannibal2.skyhanni.data.title.TitleManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.PetUtils

@SkyHanniModule
object PigPetWarning {

    private val config get() = SkyHanniMod.feature.event.yearOfThePig

    @HandleEvent
    private fun onShinyOrbUsed() {
        if (!config.petWarning) return
        if (hasPigPet()) return

        ChatUtils.clickToActionOrDisable(
            "Reminder to use a Pig Pet for the Shiny Pig!",
            config::petWarning,
            actionName = "open pets menu",
            action = { HypixelCommands.pet() },
        )
        TitleManager.sendTitle("§cPig Pet not equipped!")
    }

    // any rarity counts, comparing the internal name so a Pigman pet doesn't match
    private fun hasPigPet(): Boolean {
        val currentPet = CurrentPetApi.currentPet ?: return false
        return PetUtils.getPetProperName(currentPet.fauxInternalName) == "PIG"
    }
}
