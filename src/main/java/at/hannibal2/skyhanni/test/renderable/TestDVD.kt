package at.hannibal2.skyhanni.test.renderable

import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.animated.DVDLogoRenderable.Companion.dvdLogo
import at.hannibal2.skyhanni.utils.renderables.primitives.text

@SkyHanniModule(devOnly = true)
object TestDVD : RenderableTestSuite.TestRenderable("dvd") {

    private val dvdRenderable by lazy {
        Renderable.dvdLogo(
            renderable = Renderable.text("§zDVD Logo Test", scale = 3.0),
            movementSpeed = 1.5f,
        )
    }

    override fun renderable() = dvdRenderable
}
