import dev.detekt.api.Config
import dev.detekt.api.RequiresAnalysisApi
import java.net.URI

abstract class SkyHanniAnalysisRule(config: Config, description: String, url: URI? = null) :
    SkyHanniRule(config, description, url),
    RequiresAnalysisApi
