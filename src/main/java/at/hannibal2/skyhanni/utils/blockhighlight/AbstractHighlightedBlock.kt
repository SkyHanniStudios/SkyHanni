package at.hannibal2.skyhanni.utils.blockhighlight

import at.hannibal2.skyhanni.utils.vector.SkyHanniVector

/**
 * An abstract class that represents a location to be highlighted by a [SkyHanniBlockHighlighter]
 */
abstract class AbstractHighlightedBlock(loc: SkyHanniVector) {

    val location = loc.floor()

    /**
     * Additional criteria that the highlighted block must fulfill
     */
    abstract fun extraCondition(): Boolean

}
