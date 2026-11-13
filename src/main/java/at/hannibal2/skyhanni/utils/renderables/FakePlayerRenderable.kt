package at.hannibal2.skyhanni.utils.renderables

import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.FakePlayer
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.compat.DrawContextUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.world.entity.LivingEntity
import org.joml.Matrix3x2f
import java.awt.Color

open class FakePlayerRenderable(
    protected val player: FakePlayer,
    private val followMouse: Boolean,
    private val eyesX: Float,
    private val eyesY: Float,
    private val entityScale: Int,
    private val padding: Int,
    private val color: Color?,
    private val colorCondition: () -> Boolean,
    rawWidth: Int,
    rawHeight: Int,
) : Renderable {

    override val width = rawWidth + 2 * padding
    override val height = rawHeight + 2 * padding
    override val horizontalAlign = HorizontalAlignment.LEFT
    override val verticalAlign = VerticalAlignment.TOP

    override fun render(mouseOffsetX: Int, mouseOffsetY: Int) = drawPlayer(mouseOffsetX, mouseOffsetY)

    protected fun drawPlayer(mouseOffsetX: Int, mouseOffsetY: Int) {
        if (color != null) RenderLivingEntityHelper.setEntityColor(player, color, colorCondition)
        val mouse = Renderable.currentRenderPassMousePosition ?: return
        DrawContextUtils.pushPop {
            val peeked = DrawContextUtils.drawContext.pose().get(Matrix3x2f())
            val translationX = peeked.m20().toInt()
            val translationY = peeked.m21().toInt()
            val averageScale = (peeked.m00() + peeked.m11()) / 2
            val adjustedPadding = (padding * averageScale).toInt()
            val adjustedWidth = (width * averageScale).toInt()
            val adjustedHeight = (height * averageScale).toInt()
            drawEntityWithoutScissor(
                DrawContextUtils.drawContext,
                adjustedPadding + translationX,
                adjustedPadding + translationY,
                adjustedPadding + adjustedWidth + translationX,
                adjustedPadding + adjustedHeight + translationY,
                (entityScale * averageScale).toInt(),
                0.0625f * averageScale,
                if (followMouse) (mouse.first - mouseOffsetX.toFloat()) * averageScale + translationX else eyesX,
                if (followMouse) (mouse.second - mouseOffsetY.toFloat()) * averageScale + translationY else eyesY,
                player,
            )
        }
    }
}

fun Renderable.Companion.fakePlayer(
    player: FakePlayer,
    followMouse: Boolean = false,
    eyesX: Float = 0f,
    eyesY: Float = 0f,
    width: Int = 50,
    height: Int = 100,
    entityScale: Int = 30,
    padding: Int = 5,
    color: Color? = null,
    colorCondition: () -> Boolean = { true },
) = FakePlayerRenderable(player, followMouse, eyesX, eyesY, entityScale, padding, color, colorCondition, width, height)

internal fun drawEntityWithoutScissor(
    guiGraphics: GuiGraphicsExtractor,
    x1: Int,
    y1: Int,
    x2: Int,
    y2: Int,
    size: Int,
    scale: Float,
    mouseX: Float,
    mouseY: Float,
    entity: LivingEntity,
) {
    InventoryScreen.extractEntityInInventoryFollowsMouse(guiGraphics, x1, y1, x2, y2, size, scale, mouseX, mouseY, entity)
}
