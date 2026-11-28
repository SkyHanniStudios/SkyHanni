package at.hannibal2.skyhanni.utils.renderables.animated

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.compat.DrawContextUtils
import at.hannibal2.skyhanni.utils.compat.GuiScreenUtils
import at.hannibal2.skyhanni.utils.inPartialSeconds
import at.hannibal2.skyhanni.utils.renderables.Renderable
import kotlin.math.roundToInt
import kotlin.time.Duration

/**
 * This is a wrapper for a renderable that moves across the entire display and hits the edges and senses when the edge is hit.
 * Just like the old DVD logos on old monitors back in the old days. old.
 *
 * @param renderable The renderable to be bounced around the screen.
 * @param movementSpeed The speed in pixels per frame at a 60Hz reference rate at which the logo moves.
 * @param syncPosition A [Position] used as the initial position and kept in sync with the logo's current position each frame.
 * @param initialTrajectory The initial trajectory of the logo.
 * @param horizontalAlign The horizontal alignment of the logo on the screen.
 * @param verticalAlign The vertical alignment of the logo on the screen.
 * @param onBounce A callback that is invoked when the logo bounces off an edge.
 * @param onCornerHit A callback that is invoked when the logo hits a corner of the screen.
 */
class DVDLogoRenderable private constructor(
    private val renderable: Renderable,
    private val movementSpeed: Float = 4f,
    private val syncPosition: Position? = null,
    initialTrajectory: LogoTrajectory = LogoTrajectory.entries.random(),
    override val horizontalAlign: RenderUtils.HorizontalAlignment = RenderUtils.HorizontalAlignment.CENTER,
    override val verticalAlign: RenderUtils.VerticalAlignment = RenderUtils.VerticalAlignment.CENTER,
    private val onBounce: (Renderable) -> Unit = {},
    private val onCornerHit: (Renderable) -> Unit = {},
) : TimeDependentRenderable {
    override val width: Int = renderable.width
    override val height: Int = renderable.height

    private var x = syncPosition?.getAbsX0(width)?.toDouble() ?: randomStart(GuiScreenUtils.scaledWindowWidth, width)
    private var y = syncPosition?.getAbsY0(height)?.toDouble() ?: randomStart(GuiScreenUtils.scaledWindowHeight, height)

    var trajectory: LogoTrajectory = initialTrajectory
        private set

    override var lastRenderTime: SimpleTimeMark = SimpleTimeMark.now()

    private fun adoptMovedSyncPosition() {
        val position = syncPosition ?: return
        if (position.x == x.roundToInt() && position.y == y.roundToInt()) return
        x = position.getAbsX0(width).toDouble()
        y = position.getAbsY0(height).toDouble()
    }

    private fun move(deltaSeconds: Double) {
        val step = movementSpeed * 60.0 * deltaSeconds
        x += trajectory.x * step
        y += trajectory.y * step
    }

    private fun bounce() {
        val maxX = (GuiScreenUtils.scaledWindowWidth - width).coerceAtLeast(0).toDouble()
        val maxY = (GuiScreenUtils.scaledWindowHeight - height).coerceAtLeast(0).toDouble()
        val hitX = maxX > 0 && ((x <= 0 && trajectory.x < 0) || (x >= maxX && trajectory.x > 0))
        val hitY = maxY > 0 && ((y <= 0 && trajectory.y < 0) || (y >= maxY && trajectory.y > 0))
        x = x.coerceIn(0.0, maxX)
        y = y.coerceIn(0.0, maxY)

        if (!hitX && !hitY) return
        trajectory = trajectory.flip(hitX, hitY)
        if (hitX && hitY) onCornerHit(renderable) else onBounce(renderable)
    }

    override fun renderWithDelta(mouseOffsetX: Int, mouseOffsetY: Int, deltaTime: Duration) {
        adoptMovedSyncPosition()
        move(deltaTime.inPartialSeconds.coerceAtMost(0.1))
        bounce()

        val renderX = x.roundToInt()
        val renderY = y.roundToInt()
        syncPosition?.moveTo(renderX, renderY)
        DrawContextUtils.pushPop {
            DrawContextUtils.loadIdentity()
            DrawContextUtils.translate(renderX.toFloat(), renderY.toFloat())
            renderable.render(mouseOffsetX + renderX, mouseOffsetY + renderY)
        }
    }

    companion object {
        private fun randomStart(screenLength: Int, length: Int) = (0..(screenLength - length).coerceAtLeast(0)).random().toDouble()

        /**
         * Docs see: [DVDLogoRenderable]
         */
        fun Renderable.Companion.dvdLogo(
            renderable: Renderable,
            movementSpeed: Float = 4f,
            syncPosition: Position? = null,
            initialTrajectory: LogoTrajectory = LogoTrajectory.entries.random(),
            horizontalAlign: RenderUtils.HorizontalAlignment = RenderUtils.HorizontalAlignment.CENTER,
            verticalAlign: RenderUtils.VerticalAlignment = RenderUtils.VerticalAlignment.CENTER,
            onBounce: (Renderable) -> Unit = {},
            onCornerHit: (Renderable) -> Unit = {},
        ) = DVDLogoRenderable(
            renderable, movementSpeed, syncPosition, initialTrajectory,
            horizontalAlign, verticalAlign, onBounce, onCornerHit,
        )
    }
}

enum class LogoTrajectory(val x: Int, val y: Int) {
    UP_RIGHT(1, -1),
    UP_LEFT(-1, -1),
    DOWN_RIGHT(1, 1),
    DOWN_LEFT(-1, 1),
    ;

    fun flip(flipX: Boolean, flipY: Boolean): LogoTrajectory {
        val newX = if (flipX) -x else x
        val newY = if (flipY) -y else y
        return entries.first { it.x == newX && it.y == newY }
    }
}
