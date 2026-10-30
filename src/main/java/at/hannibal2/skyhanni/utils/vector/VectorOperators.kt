package at.hannibal2.skyhanni.utils.vector

import net.minecraft.world.phys.Vec3

object VectorOperators {

    private inline fun Vec3.map(f: (Double) -> Double) =
        SkyHanniVector(f(x), f(y), f(z))
    private inline fun Vec3.zip(other: Vec3, f: (Double, Double) -> Double) =
        SkyHanniVector(f(x, other.x), f(y, other.y), f(z, other.z))

    operator fun Vec3.component1() = x
    operator fun Vec3.component2() = y
    operator fun Vec3.component3() = z
    fun Vec3.copy(x: Number = this.x, y: Number = this.y, z: Number = this.z) = SkyHanniVector(x, y, z)

    operator fun Vec3.plus(other: Vec3) = zip(other, Double::plus)
    operator fun Vec3.minus(other: Vec3) = zip(other, Double::minus)
    operator fun Vec3.times(other: Vec3) = zip(other, Double::times)
    operator fun Vec3.times(scalar: Number) = scalar.toDouble().let { s -> map { it * s } }
    operator fun Vec3.div(scalar: Number) = scalar.toDouble().let { s -> map { it / s } }
    operator fun Vec3.unaryMinus() = map(Double::unaryMinus)
}
