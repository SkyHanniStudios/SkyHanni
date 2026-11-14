package at.hannibal2.skyhanni.test.framework

import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap

object MinecraftBootstrap {
    private val bootstrap = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    fun ensure() = bootstrap.value
}
