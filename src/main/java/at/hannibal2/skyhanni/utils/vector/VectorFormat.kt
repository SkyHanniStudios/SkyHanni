package at.hannibal2.skyhanni.utils.vector

import net.minecraft.world.phys.Vec3
import kotlin.math.round

object VectorFormat {

    fun Vec3.asStoredString() = "$x:$y:$z"
    fun Vec3.toChatFormat() = "x: ${x.toInt()}, y: ${y.toInt()}, z: ${z.toInt()}"
    fun Vec3.toLocalFormat() = "${x.toInt()} ${y.toInt()} ${z.toInt()}"
    fun Vec3.toCleanString(separator: String = ", ") = listOf(x, y, z).joinToString(separator)
    fun Vec3.formatRounded(accuracy: Int, separator: String = " "): String =
        if (accuracy == 0) listOf(x, y, z).joinToString(separator) { round(it).toInt().toString() }
        else listOf(x, y, z).joinToString(separator) { (round(it * accuracy) / accuracy).toString() }

    fun parseStored(string: String): SkyHanniVector {
        val (x, y, z) = string.split(":").map { it.toDouble() }
        return SkyHanniVector(x, y, z)
    }

    fun List<Double>.toSkyHanniVector(): SkyHanniVector {
        require(size == 3) { "Can not transform a list of size $size to SkyHanniVector" }
        return SkyHanniVector(this[0], this[1], this[2])
    }
}
