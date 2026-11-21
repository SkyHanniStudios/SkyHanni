package utils

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.annotations.KaAnnotated
import org.jetbrains.kotlin.analysis.api.annotations.KaAnnotationValue
import org.jetbrains.kotlin.analysis.api.base.KaConstantValue
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

object SkyHanniSymbols {

    private val EVENT_TYPE_ARGUMENTS = setOf("eventType", "eventTypes")

    fun KaAnnotated.hasAnnotation(classId: ClassId): Boolean = annotations.contains(classId)

    fun KaSession.isSkyHanniEvent(type: KaType): Boolean =
        (type as? KaClassType)?.classId != SkyHanniFqNames.SKYHANNI_EVENT && type.isSubtypeOf(SkyHanniFqNames.SKYHANNI_EVENT)

    fun KaSession.isSkyHanniEventClass(symbol: KaClassSymbol): Boolean = isSkyHanniEvent(symbol.defaultType)

    fun KaClassSymbol.isAbstractDeclaration(): Boolean =
        classKind == KaClassKind.INTERFACE || modality == KaSymbolModality.ABSTRACT || modality == KaSymbolModality.SEALED

    fun KaClassSymbol.displayName(): String = classId?.shortClassName?.asString() ?: "event"

    fun KaClassSymbol.primaryFunctionCandidate(): String? {
        val id = classId ?: return null
        val ownName = id.shortClassName.asString()
        val base = if (ownName.endsWith("Event")) ownName else id.outerClassId?.shortClassName?.asString() ?: return null
        return base.removeSuffix("Event").removePrefix("SkyHanni").takeIf(String::isNotEmpty)?.let { "on$it" }
    }

    fun KaAnnotated.handleEventEntry(): KtAnnotationEntry? =
        annotations[SkyHanniFqNames.HANDLE_EVENT].firstOrNull()?.psi as? KtAnnotationEntry

    fun KtAnnotationEntry.eventTypeArguments(): List<KtValueArgument> =
        valueArguments.filterIsInstance<KtValueArgument>().filterIndexed { index, argument ->
            val name = argument.getArgumentName()?.asName?.asString()
            name in EVENT_TYPE_ARGUMENTS || (name == null && index == 0)
        }

    fun KaType.displayName(): String = (this as? KaClassType)?.classId?.shortClassName?.asString() ?: "event"

    fun KaAnnotated.primaryFunctionName(): String? {
        val arguments = annotations[SkyHanniFqNames.PRIMARY_FUNCTION].firstOrNull()?.arguments ?: return null
        val argument = arguments.firstOrNull { it.name.asString() == "value" } ?: arguments.singleOrNull() ?: return null
        val constant = (argument.expression as? KaAnnotationValue.ConstantValue)?.value as? KaConstantValue.StringValue
        return constant?.value?.takeIf(String::isNotBlank)
    }

    fun KaAnnotated.explicitEventTypes(): List<ClassId> =
        annotations[SkyHanniFqNames.HANDLE_EVENT]
            .flatMap { it.arguments }
            .filter { it.name.asString() in EVENT_TYPE_ARGUMENTS }
            .flatMap { it.expression.classLiterals() }
            .filter { it != SkyHanniFqNames.SKYHANNI_EVENT }

    private fun KaAnnotationValue.classLiterals(): List<ClassId> = when (this) {
        is KaAnnotationValue.ClassLiteralValue -> listOfNotNull(classId)
        is KaAnnotationValue.ArrayValue -> values.flatMap { it.classLiterals() }
        else -> emptyList()
    }

    fun KaSession.eventParameterType(function: KtNamedFunction): KaType? =
        function.valueParameters.singleOrNull()?.returnType?.takeIf { isSkyHanniEvent(it) }

    fun KaSession.eventReceiverType(function: KtNamedFunction): KaType? =
        function.receiverTypeReference?.type?.takeIf { isSkyHanniEvent(it) }

    fun KtNamedFunction.isObjectMember(): Boolean = containingClassOrObject is KtObjectDeclaration

    @OptIn(KaExperimentalApi::class)
    fun KaSession.isReferencedInFile(function: KtNamedFunction): Boolean =
        function.containingKtFile
            .collectDescendantsOfType<KtNameReferenceExpression> { it.getReferencedName() == function.name }
            .any { it.resolveSymbol()?.psi == function }

    fun KtClassOrObject.isInSkyHanniPackage(): Boolean =
        getClassId()?.packageFqName?.startsWith(SkyHanniFqNames.ROOT_PACKAGE) == true
}
