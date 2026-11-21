package at.skyhanni.sharedvariables

enum class ProjectTarget(
    val projectName: String,
    val minecraftVersion: MinecraftVersion,
    val modrinthInfo: ModrinthInfo,
) {
    MODERN_26100(
        "26.1",
        MinecraftVersion.MC26100,
        modrinthInfo = ModrinthInfo.FABRIC_26_1,
    ),
    MODERN_26200(
        "26.2",
        MinecraftVersion.MC26200,
        modrinthInfo = ModrinthInfo.FABRIC_26_2,
    ),

    ;

    val projectPath get() = ":$projectName"

    companion object {
        fun findByMcVersion(mcVersion: String): ProjectTarget? {
            return entries.find { it.minecraftVersion.versionName == mcVersion }
        }
    }
}
