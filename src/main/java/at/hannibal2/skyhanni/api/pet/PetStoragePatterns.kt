package at.hannibal2.skyhanni.api.pet

import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
internal object PetStoragePatterns {

    private val patternGroup = RepoPattern.group("misc.pet.storage")

    /**
     * @regexTest Pets
     * @regexTest (1/3) Pets
     * @regexTest Pets (1/3)
     * @regexTest (6/6) Pets
     * @regexTest Pets: "a"
     * @regexTest Pets: "e" (1/2)
     * @regexTest (6/6) Pets: "e"
     */
    val mainPetMenuNamePattern by patternGroup.pattern(
        "menu.gui.name",
        "(?:\\(\\d+\\/\\d+\\) )?Pets(?:: \"(?<search>.*)\")?(?: \\(\\d+\\/\\d+\\))? ?",
    )

    /**
     * @regexTest Pets
     */
    val mainPetMenuTitleItemNamePattern by patternGroup.pattern(
        "menu.gui.titleitem.name",
        "Pets",
    )

    /**
     * @regexTest [Lvl 8] Squid
     * @regexTest [Lvl 100] Hermit Crab
     * @regexTest [Lvl 200] [122✦] Golden Dragon
     * @regexTest [Lvl 67] T-Rex
     */
    @Suppress("MaxLineLength")
    val petMenuPetStackNamePattern by patternGroup.pattern(
        "menu.petstack.name",
        "\\[Lvl (?<level>[\\d,]+)] (?:\\[\\d+(?<altskin>✦)] )?(?<pet>[\\w -]+?)(?<skin> ✦)?",
    )

    /**
     * @regexTestWrapped " [Lvl 100] Hedgehog"
     * @regexTestWrapped " [Lvl 68] Blaze"
     * @regexTestWrapped " [Lvl 51] Kuudra"
     * @regexTestWrapped " [Lvl 100] Flying Fish"
     * @regexTestWrapped " [Lvl 100] Chicken ✦"
     * @regexTestWrapped " [Lvl 200] [122✦] Golden Dragon"
     * @regexTestWrapped " [Lvl 67] T-Rex"
     * @regexFailWrapped " No pet selected"
     */
    @Suppress("MaxLineLength")
    val petTabWidgetNamePattern by patternGroup.pattern(
        "tab.name",
        " \\[Lvl (?<level>[\\d,]+)] (?:\\[\\d+(?<altskin>✦)\\] )?(?<pet>[\\w -]+?)(?:(?<skin> ✦))?$",
    )

    /**
     * @regexTestWrapped " +163,119,730.2 XP"
     * @regexTestWrapped " 33,915/179.7k XP (18.9%)"
     * @regexTestWrapped " 2,877.5/9.7k XP (29.7%)"
     * @regexTestWrapped " 931,886.2/1.4M XP (67.2%)"
     * @regexTestWrapped " 251,016.4/561.7k XP (44.7%)"
     * @regexTestWrapped " 3,138.4/9.7k XP (32.4%)"
     * @regexTestWrapped " MAX LEVEL"
     */
    @Suppress("MaxLineLength")
    val petTabWidgetXpPattern by patternGroup.pattern(
        "tab.xp",
        " (?:(?<max>MAX LEVEL)|(?:\\+)?(?<current>[\\d,.kM]+)(?:(?:|\\/)*(?<next>[\\d,.kM]+))? XP(?: \\((?<percentage>[\\d.]+)%\\))?)",
    )

    /**
     * @regexTest Selected pet: Chicken ✦
     * @regexTest Selected pet: Rift Ferret
     * @regexTest Selected pet: Endermite
     * @regexTest Selected pet: T-Rex
     * @regexFail Selected pet: None
     */
    val petMenuSelectedPetNamePattern by patternGroup.pattern(
        "menu.selected.name.colorless",
        "Selected pet: (?!None$)(?<pet>[\\w -]+)(?<skin> ✦)?",
    )

    /**
     * @regexTest Progress to Level 52: 29.7%
     * @regexTest Progress to Level 2: 0%
     * @regexTest Progress to Level 69: 18.9%
     * @regexTest MAX LEVEL
     */
    val petMenuSelectedPetProgressPattern by patternGroup.pattern(
        "menu.selected.progress.colorless",
        "(?:MAX LEVEL|Progress to Level (?<next>\\d+): (?<percentage>[\\d.]+)%)",
    )

    /**
     * @regexTestWrapped "                          2,877.5/9.7k"
     * @regexTestWrapped "                          33,915/179.7k"
     * @regexTestWrapped "                          931,886.2/1.4M"
     * @regexTestWrapped "                          0/660"
     * @regexTest ▸ 25,353,248 XP
     */
    val petMenuSelectedPetXpPattern by patternGroup.pattern(
        "menu.selected.xp.colorless",
        "(?:▸| )*(?<current>[\\d,.kM]+)(?: XP|(?:\\/)*(?<next>[\\d,.kM]+))",
    )

    /**
     * @regexTest §cAutopet §eequipped your §7[Lvl 100] §6Mosquito§e! §a§lVIEW RULE
     * @regexTest §cAutopet §eequipped your §7[Lvl 100] §5Rabbit§9 ✦§e! §a§lVIEW RULE
     * @regexTest §cAutopet §eequipped your §7[Lvl 200] §6[122✦] Golden Dragon§e! §a§lVIEW RULE
     * @regexTest §cAutopet §eequipped your §7[Lvl 200] §8[§6335§8§4✦§8] §6Golden Dragon§e! §a§lVIEW RULE
     */
    @Suppress("MaxLineLength")
    val autoPetMessagePattern by patternGroup.pattern(
        "autopet.message.formatted",
        "§cAutopet §eequipped your §7\\[Lvl (?<level>\\d+)] §(?<rarity>.)(?:\\[(?:§.)*\\d+(?:§.)*(?<altskin>✦)(?:§.)*] (?:§.)*)?(?<pet>[^§!]+?)(?<skin>§. ✦)?§e! §a§lVIEW RULE(?: \\(\\d+\\))?",
    )

    /**
     * @regexTest Held Item: Poignant Lucky Clover
     * @regexTest Equip: [Lvl 200] Rose Dragon Held Item: Poignant Lucky Clover
     */
    val autoPetHoverHeldItemPattern by patternGroup.pattern(
        "autopet.hover.helditem.colorless",
        ".*Held Item: (?<item>.*)",
    )

    /**
     * @regexTest Your pet is now holding Burnt Texts.
     * @regexTest Your pet is now holding Combat Exp Boost.
     * @regexTest Your pet is now holding Burnt Texts.
     */
    val petItemHeldMessagePattern by patternGroup.pattern(
        "chat.helditem",
        "Your pet is now holding (?<item>.+?)\\.",
    )

    /**
     * @regexTest You removed Quick Claw from your pet!
     */
    val petItemRemovedMessagePattern by patternGroup.pattern(
        "chat.removedhelditem",
        "You removed (?<item>.+?) from your pet!",
    )
}
