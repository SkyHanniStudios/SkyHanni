package at.hannibal2.skyhanni.events.pets

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.PetData
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction

@PrimaryFunction("onPetChange")
class PetChangeEvent(val pet: PetData) : SkyHanniEvent()
