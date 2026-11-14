package at.hannibal2.skyhanni.data.jsonobjects.local

import com.google.gson.annotations.Expose

class KnownFeaturesJson {
    @Expose
    val knownFeatures: MutableMap<String, List<String>> = mutableMapOf()
}
