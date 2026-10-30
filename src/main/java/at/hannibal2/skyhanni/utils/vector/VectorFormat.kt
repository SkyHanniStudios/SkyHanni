package at.hannibal2.skyhanni.utils.vector

import kotlin.math.round

object VectorFormat {

    fun Vector3<*>.toChatFormat() = "x: ${x.toInt()}, y: ${y.toInt()}, z: ${z.toInt()}"

    fun Vector3<*>.toLocalFormat() = "${x.toInt()} ${y.toInt()} ${z.toInt()}"

    fun Vector3<*>.toCleanString(separator: String = ", ") = listOf(x, y, z).joinToString(separator)

    fun Vector3<*>.formatRounded(accuracy: Int, separator: String = " "): String =
        if (accuracy == 0) listOf(x, y, z).joinToString(separator) { round(it).toInt().toString() }
        else listOf(x, y, z).joinToString(separator) { (round(it * accuracy) / accuracy).toString() }

    fun Vector3<*>.asStoredString() = "$x:$y:$z"

    fun parseStored(string: String): SkyHanniVector {
        val (x, y, z) = string.split(":").map { it.toDouble() }
        return SkyHanniVector(x, y, z)
    }

    fun parseStoredBlock(string: String): SkyHanniBlockVector {
        val (x, y, z) = string.split(":").map { it.toDouble().toInt() }
        return SkyHanniBlockVector(x, y, z)
    }

    fun List<Double>.toSkyHanniVector(): SkyHanniVector {
        require(size == 3) { "Can not transform a list of size $size to SkyHanniVector" }
        return SkyHanniVector(this[0], this[1], this[2])
    }

    fun DoubleArray.toSkyHanniVector(): SkyHanniVector {
        require(size == 3) { "Can not transform an array of size $size to SkyHanniVector" }
        return SkyHanniVector(this[0], this[1], this[2])
    }
}
