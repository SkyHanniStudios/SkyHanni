package events

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analysis.api.KaPlatformInterface
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.platform.declarations.createDeclarationProvider
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import utils.SkyHanniFqNames
import utils.SkyHanniSymbols.isSkyHanniEventClass
import utils.SkyHanniSymbols.primaryFunctionName

class PrimaryFunctionIndex {

    @Volatile
    private var entries: Map<String, List<ClassId>>? = null

    fun lookup(session: KaSession, project: Project, name: String): List<ClassId> =
        (entries ?: synchronized(this) { entries ?: build(session, project).also { entries = it } })[name].orEmpty()

    @OptIn(KaPlatformInterface::class)
    private fun build(session: KaSession, project: Project): Map<String, List<ClassId>> = with(session) {
        val provider = project.createDeclarationProvider(useSiteModule.contentScope, useSiteModule)
        val shortName = SkyHanniFqNames.PRIMARY_FUNCTION_ID.shortClassName.asString()
        provider.computePackageNames().orEmpty()
            .asSequence()
            .map(::FqName)
            .flatMap { pkg ->
                provider.getTopLevelKotlinClassLikeDeclarationNamesInPackage(pkg).asSequence()
                    .flatMap { provider.getAllClassesByClassId(ClassId(pkg, it)) }
            }
            .map { it.containingKtFile }
            .distinct()
            .flatMap { file ->
                file.collectDescendantsOfType<KtClassOrObject> { klass -> klass.annotationEntries.any { it.shortName?.asString() == shortName } }
            }
            .mapNotNull { declaration ->
                val symbol = declaration.classSymbol ?: return@mapNotNull null
                val classId = symbol.classId ?: return@mapNotNull null
                if (!isSkyHanniEventClass(symbol)) return@mapNotNull null
                symbol.primaryFunctionName()?.let { it to classId }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.distinct() }    }
}
