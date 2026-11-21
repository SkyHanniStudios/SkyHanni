package at.hannibal2.skyhanni.utils.vector

import kotlin.math.round

abstract class KVectorFormat(x: Double, y: Double, z: Double) : KVectorSpatial(x, y, z) {

    fun asStoredString() = "$x:$y:$z"
    fun toChatFormat() = "x: ${x.toInt()}, y: ${y.toInt()}, z: ${z.toInt()}"
    fun toLocalFormat() = "${x.toInt()} ${y.toInt()} ${z.toInt()}"
    fun toCleanString(separator: String = ", ") = listOf(x, y, z).joinToString(separator)
    fun formatRounded(accuracy: Int, separator: String = " "): String =
        if (accuracy == 0) listOf(x, y, z).joinToString(separator) { round(it).toInt().toString() }
        else listOf(x, y, z).joinToString(separator) { (round(it * accuracy) / accuracy).toString() }
}
