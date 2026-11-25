package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SkyBlockUtils
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import at.hannibal2.skyhanni.utils.json.toJsonArray
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.JsonPrimitive
import org.intellij.lang.annotations.Language

@SkyHanniModule
object ChatSoundResponse {
    private val config get() = SkyHanniMod.feature.chat.soundResponse

    init {
        SoundResponseTypes.entries.forEach { it.pattern }
    }

    @HandleEvent
    private fun onChat(event: SkyHanniChatEvent.Allow) {
        if (!isEnabled()) return

        for (soundType in SoundResponseTypes.entries) {
            if (!config.soundResponses.contains(soundType)) continue
            if (soundType.pattern.matches(event.message)) {
                soundType.sound.playSound()
                return
            }
        }
    }

    @HandleEvent
    private fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(95, "chat.soundResponse.soundResponses") { element ->
            if (!element.isJsonArray) return@transform element
            val array = element.asJsonArray
            mapOf(
                JsonPrimitive("CAT") to lazy {
                    listOf(
                        JsonPrimitive("CATPURR"),
                        JsonPrimitive("CATPURREOW"),
                        JsonPrimitive("CATHISS"),
                    ).toJsonArray()
                },
                JsonPrimitive("DOG") to lazy {
                    listOf(
                        JsonPrimitive("DOGGROWL"),
                        JsonPrimitive("DOGHOWL"),
                    ).toJsonArray()
                },
            ).forEach {
                if (array.contains(it.key)) {
                    array.addAll(it.value.value)
                }
            }
            array
        }
        event.transform(117, "chat.soundResponse.soundResponses") { element ->
            element.asJsonArray.apply {
                add(JsonPrimitive("FROG"))
                add(JsonPrimitive("HORSE"))
                add(JsonPrimitive("GOAT"))
            }
        }
    }

    fun isEnabled() = SkyBlockUtils.inSkyBlock && config.enabled
}

@Language("RegExp")
private const val START_PATTERN = "(?:^|^.* )(?: |§.)*(?i)"
@Language("RegExp")
private const val END_PATTERN = "(?: |§.|!|\\?|\\.)*(?:\$| .*\$)"

enum class SoundResponseTypes(private val displayName: String, soundLocation: String, @Language("RegExp") vararg triggersOn: String) {
    CAT("Cat Meow", "entity.cat.ambient", "m+e*o*w+", "m+e*a+o+w+"),
    CATPURR("Cat Purr", "entity.cat.purr", "p+u*rr+"),
    CATPURREOW("Cat Purreow", "entity.cat.purreow", "m+r+e*o*w+", "m+r+e*a+o+w+"),
    CATHISS("Cat Hiss", "entity.cat.hiss", "h+i+ss+"),
    DOG("Dog Bark", "entity.wolf.ambient", "bark", "a*w*r+u*f+", "w+oo+f+"),
    DOGGROWL("Dog Growl", "entity.wolf.growl", "g+rr+"),
    DOGHOWL("Dog Howl", "entity.wolf.death", "a+w+oo+"),
    SHEEP("Sheep", "entity.sheep.ambient", "baa+h*"),
    COW("Cow", "entity.cow.ambient", "moo+"),
    PIG("Pig", "entity.pig.ambient", "o+i+n+k+"),
    CHICKEN("Chicken", "entity.chicken.ambient", "cl+u+c+k+"),
    FROG("Frog", "entity.frog.hurt", "ribbit"),
    HORSE("Horse", "entity.horse.angry", "neigh"),
    GOAT("Goat", "entity.goat.screaming.ambient", "bleat"),
    FOX("Fox", "entity.fox.ambient", "y+i+p+"),
    ;

    val sound by lazy { SoundUtils.createSound(soundLocation, 1f, isWarning = false) }

    // creates a pattern that looks for if the message contains any of the triggerOn strings but as a full word
    @Suppress("RegExpUnnecessaryNonCapturingGroup")
    val pattern by RepoPattern.pattern(
        "chat.sound.response" + name.lowercase(),
        "$START_PATTERN(?:${triggersOn.joinToString("|")})$END_PATTERN",
    )

    override fun toString(): String = displayName
}
