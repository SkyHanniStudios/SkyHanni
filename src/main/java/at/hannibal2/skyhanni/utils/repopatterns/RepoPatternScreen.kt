package at.hannibal2.skyhanni.utils.repopatterns

import at.hannibal2.skyhanni.data.model.TextInput
import at.hannibal2.skyhanni.utils.compat.SkyHanniChromeScreen
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.ScrollValue

class RepoPatternScreen(
    val allPatterns: List<CommonPatternInfo<*, *>>,
) : SkyHanniChromeScreen() {

    override val screenTitle = "Repo Patterns"

    internal val searchInput = TextInput()
    internal val scrollValue = ScrollValue()
    internal var isSearchActive = false

    override fun buildContent(): Renderable = RepoPatternGui.buildContent(this)
}
