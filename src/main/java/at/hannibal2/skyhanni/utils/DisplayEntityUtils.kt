package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.vector.SkyHanniVector
import at.hannibal2.skyhanni.utils.vector.VectorConversions.toSkyHanniVector
import com.mojang.math.Transformation
import net.minecraft.world.entity.Display
import org.joml.Vector3f

object DisplayEntityUtils {
    val Display.transformation: Transformation?
        get() {
            return renderState()?.transformation?.get(0f)
        }

    fun Display.TextDisplay.arrowForwardVec(): SkyHanniVector {
        val quat = transformation?.leftRotation() ?: return SkyHanniVector(0, 0, 1)
        val localY = Vector3f(0f, 1f, 0f)
        quat.transform(localY)
        return SkyHanniVector(localY.x, 0.0, localY.z).normalize().toSkyHanniVector()
    }

    val Transformation.uniformScale: Float?
        get() {
            val scaleTransform = this.scale()
            val scale = scaleTransform.x()
            return if (scale == scaleTransform.y() && scale == scaleTransform.z()) scale else null
        }
}
