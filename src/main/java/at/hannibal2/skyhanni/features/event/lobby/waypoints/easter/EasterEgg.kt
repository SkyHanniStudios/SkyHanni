package at.hannibal2.skyhanni.features.event.lobby.waypoints.easter

import at.hannibal2.skyhanni.utils.vector.SkyHanniVector

enum class EasterEgg(val eggName: String, val waypoint: SkyHanniVector) {
    EASTER_EGG_1("#1", SkyHanniVector(-45, 93, 3)),
    EASTER_EGG_2("#2", SkyHanniVector(-27, 80, 8)),
    EASTER_EGG_3("#3", SkyHanniVector(-12, 65, 3)),
    EASTER_EGG_4("#4", SkyHanniVector(20, 68, -30)),
    EASTER_EGG_5("#5", SkyHanniVector(19, 58, -45)),
    EASTER_EGG_6("#6", SkyHanniVector(35, 64, 20)),
    EASTER_EGG_7("#7", SkyHanniVector(85, 67, -27)),
    EASTER_EGG_8("#8", SkyHanniVector(91, 63, -117)),
    EASTER_EGG_9("#9", SkyHanniVector(129, 71, -141)),
    EASTER_EGG_10("#10", SkyHanniVector(69, 60, -165)),
    EASTER_EGG_11("#11", SkyHanniVector(67, 105, -137)),
    EASTER_EGG_12("#12", SkyHanniVector(-5, 61, -182)),
    EASTER_EGG_13("#13", SkyHanniVector(-123, 55, -182)),
    EASTER_EGG_14("#14", SkyHanniVector(-127, 70, -180)),
    EASTER_EGG_15("#15", SkyHanniVector(-158, 66, -130)),
    EASTER_EGG_16("#16", SkyHanniVector(-179, 82, -16)),
    EASTER_EGG_17("#17", SkyHanniVector(-140, 100, 0)),
    EASTER_EGG_18("#18", SkyHanniVector(-48, 87, 30)),
    EASTER_EGG_19("#19", SkyHanniVector(-43, 87, 39)),
    EASTER_EGG_20("#20", SkyHanniVector(-15, 65, 109)),
    EASTER_EGG_21("#21", SkyHanniVector(34, 59, 118)),
    EASTER_EGG_22("#22", SkyHanniVector(49, 65, 91)),
    EASTER_EGG_23("#23", SkyHanniVector(96, 69, 120)),
    EASTER_EGG_24("#24", SkyHanniVector(162, 60, 130)),
    EASTER_EGG_25("#25", SkyHanniVector(177, 96, -6)),
    EASTER_EGG_26("#26", SkyHanniVector(188, 57, -46)),
    EASTER_EGG_27("#27", SkyHanniVector(-47, 65, 152)),
    EASTER_EGG_28("#28", SkyHanniVector(-71, 61, 203)),
    EASTER_EGG_29("#29", SkyHanniVector(-68, 54, 243)),
    EASTER_EGG_30("#30", SkyHanniVector(55, 64, 251)),
    ;

    var found = false
}
