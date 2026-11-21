package utils

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import kotlin.reflect.KClass

object DetektUtils {

    fun KtAnnotated.hasAnnotation(name: String): Boolean =
        this.getAnnotation(name) != null

    fun KtAnnotated.getAnnotation(name: String): KtAnnotationEntry? =
        annotationEntries.find { it.shortName?.asString() == name }

    inline fun <reified T : Annotation> KtAnnotated.hasAnnotation(): Boolean = hasAnnotation(T::class.java.simpleName)

    inline fun <reified T : Annotation> KtAnnotated.getAnnotation(): KtAnnotationEntry? = getAnnotation(T::class.java.simpleName)

    fun KClass<*>.javaClassId(): ClassId = with(java) {
        ClassId(FqName(packageName), FqName(name.removePrefix("$packageName.").replace('$', '.')), false)
    }

    // Skip:
    //  - Local properties
    //  - Private properties
    //  - Properties with getters
    //  - Properties with @Transient annotation
    fun KtProperty.doWeNeedToCheckConfigProp(): Boolean {
        val hasExplicitGetter = getter?.hasBody() ?: false
        val isTransient = hasAnnotation<Transient>()

        return !isLocal && !isPrivate() && !hasExplicitGetter && !isTransient
    }

}
