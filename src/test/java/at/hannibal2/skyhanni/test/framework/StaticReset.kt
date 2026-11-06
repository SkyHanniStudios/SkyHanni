package at.hannibal2.skyhanni.test.framework

import at.hannibal2.skyhanni.utils.ItemNameResolver
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.repopatterns.RepoPatternManager

object StaticReset {
    private val resets = mutableListOf<() -> Unit>(
        NeuInternalName::clearItemNameCache,
        ItemNameResolver::clearCache,
        { RepoPatternManager.inTestDuplicateUsage = true },
    )

    fun register(reset: () -> Unit) {
        resets.add(reset)
    }

    fun resetAll() = resets.forEach { it() }
}
