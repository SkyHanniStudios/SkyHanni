package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ParkourJson(
    @Expose val locations: List<SkyHanniVector>,
    @Expose @SerializedName(value = "short_cuts", alternate = ["shortCuts"]) val shortCuts: List<ParkourShortCut> = listOf(),
)

data class ParkourShortCut(
    @Expose val from: Int,
    @Expose val to: Int,
)
