package at.hannibal2.skyhanni.features.achievements

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.achievements.Achievement
import at.hannibal2.skyhanni.events.achievements.AchievementRegistrationEvent
import at.hannibal2.skyhanni.events.player.PlayerDeathEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule

@SkyHanniModule
object DianaAchievements {

    private const val INQ_ACHIEVEMENT = "Death Via Inq"

    @HandleEvent
    private fun onAchievementRegistration(event: AchievementRegistrationEvent) {
        val achievement = Achievement(
            name = "That was that book...",
            description = "Die to an Inquisitor",
            userLuckAmount = 5f,
        )
        event.register(achievement, INQ_ACHIEVEMENT)
    }

    @HandleEvent(onlyOnSkyblock = true)
    private fun onPlayerDeath(event: PlayerDeathEvent.Allow) {
        if (!event.isSelf) return
        if (!event.reason.contains("Minos Inquisitor")) return
        AchievementManager.completeAchievement(INQ_ACHIEVEMENT)
    }
}
