package at.hannibal2.skyhanni.utils.vector

import net.minecraft.world.phys.Vec3

// Vec3 equals and hashcode use non-equivalent negative zeroes, so these `+ .0` are important.
class SkyHanniVector(x: Double, y: Double, z: Double) : Vec3(x + 0.0, y + 0.0, z + 0.0) {

    constructor(x: Number, y: Number, z: Number) : this(x.toDouble(), y.toDouble(), z.toDouble())
    constructor(vec: Vec3) : this(vec.x, vec.y, vec.z)

    override fun add(x: Double, y: Double, z: Double) = SkyHanniVector(this.x + x, this.y + y, this.z + z)
    fun add(x: Number = 0, y: Number = 0, z: Number = 0) = add(x.toDouble(), y.toDouble(), z.toDouble())
    fun up(offset: Number = 1) = add(y = offset)
    fun down(offset: Number = 1) = up(-offset.toDouble())

    override fun equals(other: Any?) = other is Vec3 && x == other.x && y == other.y && z == other.z
    override fun hashCode() = super.hashCode()

    companion object {
        val ZERO = SkyHanniVector(0, 0, 0)
    }
}
