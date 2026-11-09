package events

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPackageSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.isSkyHanniEventClass
import utils.SkyHanniSymbols.primaryFunctionName

class PrimaryFunctionIndex {

    @Volatile
    private var entries: Map<String, List<ClassId>>? = null

    fun lookup(session: KaSession, name: String): List<ClassId>? = index(session)?.let { it[name].orEmpty() }

    private fun index(session: KaSession): Map<String, List<ClassId>>? =
        entries ?: synchronized(this) { entries ?: build(session).takeIf { it.isNotEmpty() }?.also { entries = it } }

    private fun build(session: KaSession): Map<String, List<ClassId>> = with(session) {
        val root = findPackage(FqName(SkyHanniFqNames.ROOT_PACKAGE)) ?: return emptyMap()
        val classes = mutableListOf<KaClassSymbol>()
        collectPackage(root, classes)
        classes.mapNotNull { symbol ->
            val classId = symbol.classId ?: return@mapNotNull null
            val name = symbol.primaryFunctionName() ?: return@mapNotNull null
            (name to classId).takeIf { isSkyHanniEventClass(symbol) }
        }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.distinct() }
    }

    @OptIn(KaExperimentalApi::class)
    private fun KaSession.collectPackage(pkg: KaPackageSymbol, target: MutableList<KaClassSymbol>) {
        val scope = pkg.packageScope
        scope.classifiers.filterIsInstance<KaClassSymbol>().forEach { collectClass(it, target) }
        scope.getPackageSymbols { true }.forEach { collectPackage(it, target) }
    }

    private fun KaSession.collectClass(symbol: KaClassSymbol, target: MutableList<KaClassSymbol>) {
        target += symbol
        symbol.combinedDeclaredMemberScope.classifiers.filterIsInstance<KaClassSymbol>().forEach { collectClass(it, target) }
    }
}
