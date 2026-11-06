package at.hannibal2.skyhanni.utils.vector

import kotlin.math.floor

object VectorBlocks {

    val AXIS_DIRECTIONS = setOf(
        SkyHanniBlockVector(1, 0, 0),
        SkyHanniBlockVector(-1, 0, 0),
        SkyHanniBlockVector(0, 1, 0),
        SkyHanniBlockVector(0, -1, 0),
        SkyHanniBlockVector(0, 0, 1),
        SkyHanniBlockVector(0, 0, -1),
    )

    fun Vector3<*>.floorToBlock() = SkyHanniBlockVector(floor(x).toInt(), floor(y).toInt(), floor(z).toInt())

    fun <V : VectorArithmetic<V>> VectorArithmetic<V>.blockCenter() = floor().addHalf()

    fun SkyHanniBlockVector.neighbors() = AXIS_DIRECTIONS.map { this + it }
}
