package at.hannibal2.skyhanni.features.misc.trevor

import at.hannibal2.skyhanni.utils.vector.SkyHanniVector

enum class TrapperMobArea(val location: String, val coordinates: SkyHanniVector) {
    OASIS("Oasis", SkyHanniVector(126.0, 77.0, -456.0)),
    GORGE("Mushroom Gorge", SkyHanniVector(300.0, 80.0, -509.0)),
    OVERGROWN("Overgrown Mushroom Cave", SkyHanniVector(242.0, 60.0, -389.0)),
    SETTLEMENT("Desert Settlement", SkyHanniVector(184.0, 86.0, -384.0)),
    GLOWING("Glowing Mushroom Cave", SkyHanniVector(199.0, 50.0, -512.0)),
    MOUNTAIN("Desert Mountain", SkyHanniVector(255.0, 148.0, -518.0)),
    FOUND("    ", SkyHanniVector(0.0, 0.0, 0.0)),
    NONE("   ", SkyHanniVector(0.0, 0.0, 0.0)),
}
