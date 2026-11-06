package at.hannibal2.skyhanni.test.framework

import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap

object MinecraftBootstrap {
    @Volatile
    private var bootstrapped = false

    fun ensure() {
        if (bootstrapped) return
        synchronized(this) {
            if (bootstrapped) return
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
            bootstrapped = true
        }
    }
}
