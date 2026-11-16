package at.hannibal2.skyhanni.utils.compat

import net.minecraft.network.chat.Component

/**
 * Base class for full-canvas screens that manage the entire display surface
 * (e.g., drag-and-drop editors). No chrome is applied.
 */
@Suppress("AbstractClassCanBeInterface")
abstract class SkyHanniCanvasScreen(title: Component = Component.empty()) : SkyHanniBaseScreen(title)
