package at.hannibal2.skyhanni.utils.vector

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

@JvmInline
value class SkyHanniBlockVector private constructor(private val packed: Long) {

    constructor(x: Int, y: Int, z: Int) : this(pack(x, y, z))

    val x: Int get() = (packed shr X_OFFSET).toInt()
    val y: Int get() = (packed shl (Long.SIZE_BITS - Y_BITS) shr (Long.SIZE_BITS - Y_BITS)).toInt()
    val z: Int get() = (packed shl (Long.SIZE_BITS - Z_OFFSET - Z_BITS) shr (Long.SIZE_BITS - Z_BITS)).toInt()

    operator fun component1() = x
    operator fun component2() = y
    operator fun component3() = z

    fun asLong() = packed

    operator fun plus(other: SkyHanniBlockVector) = SkyHanniBlockVector(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: SkyHanniBlockVector) = SkyHanniBlockVector(x - other.x, y - other.y, z - other.z)

    fun offset(x: Int = 0, y: Int = 0, z: Int = 0) = SkyHanniBlockVector(this.x + x, this.y + y, this.z + z)

    fun up(amount: Int = 1) = offset(y = amount)

    fun down(amount: Int = 1) = offset(y = -amount)

    fun distanceSq(other: SkyHanniBlockVector): Long {
        val dx = (other.x - x).toLong()
        val dy = (other.y - y).toLong()
        val dz = (other.z - z).toLong()
        return dx * dx + dy * dy + dz * dz
    }

    fun distance(other: SkyHanniBlockVector) = sqrt(distanceSq(other).toDouble())

    fun distanceChebyshev(other: SkyHanniBlockVector) = max(abs(x - other.x), max(abs(y - other.y), abs(z - other.z)))

    fun toVector() = SkyHanniVector(x, y, z)

    fun center() = toVector().addHalf()

    fun toChatFormat() = "x: $x, y: $y, z: $z"

    fun toLocalFormat() = "$x $y $z"

    fun asStoredString() = "$x:$y:$z"

    override fun toString() = "SkyHanniBlockVector(x=$x, y=$y, z=$z)"

    companion object {
        private const val X_BITS = 26
        private const val Z_BITS = 26
        private const val Y_BITS = Long.SIZE_BITS - X_BITS - Z_BITS
        private const val Z_OFFSET = Y_BITS
        private const val X_OFFSET = Y_BITS + Z_BITS
        private const val X_MASK = (1L shl X_BITS) - 1
        private const val Y_MASK = (1L shl Y_BITS) - 1
        private const val Z_MASK = (1L shl Z_BITS) - 1

        val ZERO = SkyHanniBlockVector(0, 0, 0)

        fun fromPacked(packed: Long) = SkyHanniBlockVector(packed)

        private fun pack(x: Int, y: Int, z: Int): Long =
            (x.toLong() and X_MASK shl X_OFFSET) or (y.toLong() and Y_MASK) or (z.toLong() and Z_MASK shl Z_OFFSET)
    }
}
