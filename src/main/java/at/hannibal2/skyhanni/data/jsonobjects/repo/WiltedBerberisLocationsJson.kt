package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class WiltedBerberisLocationsJson(
    @Expose @SerializedName("field_centers") val fieldCenters: List<FieldCenter>,
)

data class FieldCenter(
    @Expose val position: SkyHanniVector,
    @Expose val count: Int,
)
