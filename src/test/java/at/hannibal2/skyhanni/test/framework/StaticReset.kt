package at.hannibal2.skyhanni.test.framework

import at.hannibal2.skyhanni.utils.ItemNameResolver
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.repopatterns.RepoPatternManager

/**
 * Hooks run after every test to clear static caches and test flags in main code
 */
object StaticReset {
    private val resets = mutableListOf(
        NeuInternalName::clearItemNameCache,
        ItemNameResolver::clearCache,
        { RepoPatternManager.inTestDuplicateUsage = true },
    )

    fun register(reset: () -> Unit) {
        resets.add(reset)
    }

    fun resetAll() = resets.forEach { it() }
}
