package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.achievements.Achievement
import at.hannibal2.skyhanni.events.achievements.AchievementRegistrationEvent
import at.hannibal2.skyhanni.features.achievements.AchievementManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import at.hannibal2.skyhanni.utils.chat.TextHelper.asComponent
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.animated.DVDLogoRenderable
import at.hannibal2.skyhanni.utils.renderables.animated.DVDLogoRenderable.Companion.dvdLogo
import at.hannibal2.skyhanni.utils.renderables.animated.LogoTrajectory
import at.hannibal2.skyhanni.utils.renderables.container.VerticalContainerRenderable.Companion.vertical
import at.hannibal2.skyhanni.utils.renderables.primitives.text

@SkyHanniModule
object DVDLogoDisplay {

    private val config get() = SkyHanniMod.feature.misc.dvdLogo
    private var renderable: DVDLogoRenderable? = null
    private var renderableDirty = true

    private const val DVD_ACHIEVEMENT = "DVD Logo Corner Hits"
    private fun handleCornerHit() = with(AchievementManager) {
        val current = getAchievement(DVD_ACHIEVEMENT)
        updateTieredAchievement(DVD_ACHIEVEMENT, current.data.progress + 1)
    }

    private fun buildRenderable() {
        val startTrajectory = renderable?.trajectory ?: LogoTrajectory.entries.random()

        // Capture here instead of at runtime
        val edgeSound = config.edgeHitSound.get().takeIf { it.isNotBlank() }?.let {
            SoundUtils.createSound(it, 1f, isWarning = false)
        }
        val cornerSound = config.cornerHitSound.get().takeIf { it.isNotBlank() }?.let {
            SoundUtils.createSound(it, 1f, isWarning = false)
        }

        val lines = config.text.get().replace("&", "§").split("\\n").map {
            Renderable.text(it, scale = config.textSize.get().toDouble(), horizontalAlign = RenderUtils.HorizontalAlignment.CENTER)
        }

        renderable = Renderable.dvdLogo(
            Renderable.vertical(lines, horizontalAlign = RenderUtils.HorizontalAlignment.CENTER),
            movementSpeed = config.speed.get(),
            syncPosition = config.position,
            initialTrajectory = startTrajectory,
            onBounce = { edgeSound?.playSound() },
            onCornerHit = {
                cornerSound?.playSound()
                handleCornerHit()
            },
        )
    }

    @HandleEvent
    private fun onConfigLoad() {
        ConditionalUtils.onAnyToggled(config) {
            renderableDirty = true
        }
    }

    @HandleEvent
    private fun onGuiRenderTop() {
        if (!config.enabled.get()) return
        if (renderableDirty) {
            buildRenderable()
            renderableDirty = false
        }
        val renderable = renderable ?: return
        config.position.renderRenderable(renderable, posLabel = "DVD Logo")
    }

    @HandleEvent
    private fun onAchievementRegistration(event: AchievementRegistrationEvent) {
        val achievement = Achievement(
            name = "Corner Connoisseur".asComponent(),
            description = "Have the DVD Logo hit a corner perfectly".asComponent(),
            userLuckAmount = 1f,
            tiers = listOf(1, 5, 10),
            hidden = true,
        )
        event.register(achievement, DVD_ACHIEVEMENT)
    }
}
