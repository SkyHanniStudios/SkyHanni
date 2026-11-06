package at.hannibal2.skyhanni.utils.vector

@JvmRecord
data class SkyHanniVector(
    override val x: Double,
    override val y: Double,
    override val z: Double,
) : VectorMetrics<SkyHanniVector>, LegacyVectorMath<SkyHanniVector>, LegacyVectorHelpers<SkyHanniVector> {

    constructor(x: Number, y: Number, z: Number) : this(x.toDouble(), y.toDouble(), z.toDouble())

    override fun of(x: Double, y: Double, z: Double) = SkyHanniVector(x, y, z)

    override val self get() = this

    override fun equals(other: Any?) =
        this === other || other is SkyHanniVector && x == other.x && y == other.y && z == other.z

    override fun hashCode(): Int {
        var result = (x + 0.0).hashCode()
        result = 31 * result + (y + 0.0).hashCode()
        result = 31 * result + (z + 0.0).hashCode()
        return result
    }

    companion object {
        val ZERO = SkyHanniVector(0.0, 0.0, 0.0)
        val X_AXIS = SkyHanniVector(1.0, 0.0, 0.0)
        val Y_AXIS = SkyHanniVector(0.0, 1.0, 0.0)
        val Z_AXIS = SkyHanniVector(0.0, 0.0, 1.0)
    }
}
