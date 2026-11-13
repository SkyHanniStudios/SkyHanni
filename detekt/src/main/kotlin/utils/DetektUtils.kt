package utils

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import kotlin.reflect.KClass

object DetektUtils {

    fun KtAnnotationEntry.resolvedClassId(): ClassId? = analyze(this) { (typeReference?.type as? KaClassType)?.classId }

    fun KtAnnotated.getAnnotation(classId: ClassId): KtAnnotationEntry? =
        annotationEntries.firstOrNull { it.resolvedClassId() == classId }

    fun KtAnnotated.hasAnnotation(classId: ClassId): Boolean = getAnnotation(classId) != null

    inline fun <reified T : Annotation> KtAnnotated.getAnnotation(): KtAnnotationEntry? = getAnnotation(T::class.javaClassId())

    inline fun <reified T : Annotation> KtAnnotated.hasAnnotation(): Boolean = hasAnnotation(T::class.javaClassId())

    fun KClass<*>.javaClassId(): ClassId = with(java) {
        ClassId(FqName(packageName), FqName(name.removePrefix("$packageName.").replace('$', '.')), false)
    }

    fun KtFile.isConfigOrStorageFile(): Boolean =
        packageFqName.startsWith(SkyHanniFqNames.CONFIG_FEATURES_PACKAGE) ||
            packageFqName.startsWith(SkyHanniFqNames.CONFIG_STORAGE_PACKAGE)

    // Skip:
    //  - Local properties
    //  - Private properties
    //  - Properties with getters
    //  - Properties with @Transient annotation
    fun KtProperty.doWeNeedToCheckConfigProp(): Boolean {
        val hasExplicitGetter = getter?.hasBody() ?: false
        return !isLocal && !isPrivate() && !hasExplicitGetter && !hasAnnotation<Transient>()
    }

}
