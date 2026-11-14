package at.hannibal2.skyhanni.utils.renderables

import at.hannibal2.skyhanni.utils.renderables.container.VerticalContainerRenderable.Companion.vertical
import at.hannibal2.skyhanni.utils.renderables.primitives.text

/**
 * A bordered card with a title label above a body Renderable.
 *
 * @param title The card title; may contain Minecraft color codes.
 * @param body The content to display inside the card.
 * @param width The fixed total width of the card.
 */
fun Renderable.Companion.labeledCard(
    title: String,
    body: Renderable,
    width: Int,
): Renderable = Renderable.vertical(
    listOf(Renderable.fixedSizeLine(Renderable.text(title), width), Renderable.fixedSizeLine(body, width)),
    spacing = 4,
)
