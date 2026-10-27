package at.hannibal2.skyhanni.data.model.waypoints

import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import com.google.gson.annotations.Expose

class SkyHanniWaypoint(
    @Expose
    val location: SkyHanniVector,
    @Expose
    var number: Int,
    @Expose
    val options: MutableMap<String, String> = mutableMapOf(),
) : Copyable<SkyHanniWaypoint> {
    override fun copy() = SkyHanniWaypoint(location, number, options)
}
