package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import com.google.gson.annotations.Expose

data class RiftWoodenButtonsJson(
    @Expose val houses: Map<String, List<ButtonSpots>>,
)

data class ButtonSpots(
    @Expose val position: SkyHanniVector,
    @Expose val buttons: List<SkyHanniVector>,
)
