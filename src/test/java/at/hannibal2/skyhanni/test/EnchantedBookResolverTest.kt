package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.utils.ItemNameResolver
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MinecraftTestBootstrapExtension::class)
class EnchantedBookResolverTest {

    private fun firstCandidate(displayName: String) = ItemNameResolver.enchantedBookCandidates(displayName).firstOrNull()

    @Test
    fun normalBook() {
        assertEquals("LOOTING;4".toInternalName(), firstCandidate("§9Looting IV"))
    }

    @Test
    fun ultimateBook() {
        assertEquals("ULTIMATE_CHIMERA;5".toInternalName(), firstCandidate("§d§lChimera V"))
    }

    @Test
    fun ultimateWiseKeepsItsName() {
        assertEquals("ULTIMATE_WISE;5".toInternalName(), firstCandidate("§d§lUltimate Wise V"))
    }

    @Test
    fun paleontologistBook() {
        assertEquals("PALEONTOLOGIST;3".toInternalName(), firstCandidate("§9Paleontologist III"))
    }

    @Test
    fun bookWrapperIsUnwrapped() {
        assertEquals("LUCK;7".toInternalName(), firstCandidate("§fEnchanted Book (§5Luck VII§f)"))
    }

    @Test
    fun duplexBecomesReiterate() {
        assertEquals("ULTIMATE_REITERATE;3".toInternalName(), firstCandidate("§d§lDuplex III"))
        assertEquals("ULTIMATE_REITERATE;5".toInternalName(), ItemNameResolver.fixEnchantmentName("ULTIMATE_DUPLEX;5"))
    }

    @Test
    fun newSuffixIsStripped() {
        assertEquals("LOOTING;7".toInternalName(), ItemNameResolver.fixEnchantmentName("LOOTING_NEW;7"))
        assertEquals("LUCK;7".toInternalName(), ItemNameResolver.fixEnchantmentName("luck_new;7"))
    }

    @Test
    fun newSuffixOnlyStrippedFromEnchantIds() {
        assertEquals("SALMON_HELMET_NEW".toInternalName(), ItemNameResolver.fixEnchantmentName("SALMON_HELMET_NEW"))
    }

    @Test
    fun doubleUltimatePrefixIsCollapsed() {
        assertEquals("ULTIMATE_LEGION;1".toInternalName(), ItemNameResolver.fixEnchantmentName("ULTIMATE_ULTIMATE_LEGION;1"))
    }

    @Test
    fun unknownBookResolvesToNull() {
        assertTrue(ItemNameResolver.enchantedBookCandidates("§7Hay Bale").isEmpty())
        assertNull(ItemNameResolver.resolveEnchantedBook("§9Nonexistent Enchant II"))
    }
}
