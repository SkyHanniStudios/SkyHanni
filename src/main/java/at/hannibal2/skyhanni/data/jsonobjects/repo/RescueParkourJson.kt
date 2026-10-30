package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import com.google.gson.annotations.Expose

class RescueParkourJson(
    @Expose val mage: Map<String, List<SkyHanniVector>>,
    @Expose val barb: Map<String, List<SkyHanniVector>>,
)
