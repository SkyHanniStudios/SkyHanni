import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency
import java.util.Optional

// Credit to SkyOcean
data class ForwardingVersionCatalog(
    val catalogs: List<VersionCatalog>,
) {
    constructor(vararg catalogs: VersionCatalog) : this(listOf(*catalogs))

    private fun <T> first(name: String, lookup: VersionCatalog.(String) -> Optional<T>): T =
        catalogs.firstNotNullOf { it.lookup(name).orElse(null) }

    val libraries = ForwardingProperty(this, VersionCatalog::findLibrary)
    val bundles = ForwardingProperty(this, VersionCatalog::findBundle)
    val plugins = ForwardingProperty(this, VersionCatalog::findPlugin)
    val versions = ForwardingProperty(this, VersionCatalog::findVersion)

    fun library(name: String): Provider<MinimalExternalModuleDependency> = first(name, VersionCatalog::findLibrary)
    fun bundle(name: String): Provider<ExternalModuleDependencyBundle> = first(name, VersionCatalog::findBundle)
    fun plugin(name: String): Provider<PluginDependency> = first(name, VersionCatalog::findPlugin)
    fun version(name: String): VersionConstraint = first(name, VersionCatalog::findVersion)

    operator fun get(name: String): Provider<MinimalExternalModuleDependency> = library(name)

    data class ForwardingProperty<T>(
        val parent: ForwardingVersionCatalog,
        val lookup: VersionCatalog.(String) -> Optional<T>,
    ) {
        operator fun get(name: String): T = parent.first(name, lookup)
        fun has(name: String): Boolean = runCatching { get(name) }.isSuccess
        fun getOrNull(name: String): T? = runCatching { get(name) }.getOrNull()
    }
}

internal val entries = mutableMapOf<Project, ForwardingVersionCatalog>()
val Project.versionedCatalog get() = entries[this] ?: ForwardingVersionCatalog()
