package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LocationFixJson(
    @Expose @SerializedName(value = "location_fixes", alternate = ["locationFixes"]) val locationFixes: Map<String, LocationFix>,
)

data class LocationFix(
    @Expose val a: SkyHanniVector,
    @Expose val b: SkyHanniVector,
    @Expose @SerializedName("island_name") val islandName: String,
    @Expose @SerializedName("real_location") val realLocation: String,
)
