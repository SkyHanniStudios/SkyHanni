package at.hannibal2.skyhanni.utils.json

import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.utils.ReflectionUtils.getPrivateField
import at.hannibal2.skyhanni.utils.ReflectionUtils.getPrivateFieldValue
import com.google.gson.JsonElement
import io.github.notenoughupdates.moulconfig.observer.Property
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

// Copied (+ adapted to Kotlin) from NEU
class Shimmy private constructor(
    private val type: Type,
    private val getter: () -> Any?,
    private val setter: (Any?) -> Unit,
) {
    fun getJson(): JsonElement = ConfigManager.gson.toJsonTree(getter())

    fun setJson(element: JsonElement) {
        setter(ConfigManager.gson.fromJson(element, type))
    }

    companion object {
        private fun traverse(source: Any?, fieldName: String): Any? =
            runCatching { source?.getPrivateFieldValue(fieldName) }.getOrNull()

        operator fun invoke(source: Any?, path: List<String>): Shimmy? {
            if (path.isEmpty()) return null
            val parent = path.dropLast(1).fold(source) { obj, part -> traverse(obj, part) } ?: return null
            val field = try {
                parent.getPrivateField(path.last())
            } catch (_: NoSuchFieldException) {
                return null
            }
            // genericType keeps type arguments, so a list of enums stays a list of enums
            val fieldShimmy = Shimmy(field.genericType, { field.get(parent) }, { field.set(parent, it) })
            if (field.type != Property::class.java) return fieldShimmy

            @Suppress("UNCHECKED_CAST")
            val property = field.get(parent) as? Property<Any?> ?: return fieldShimmy
            val valueType = (field.genericType as? ParameterizedType)?.actualTypeArguments?.first() ?: Any::class.java
            // Property.set notifies the observers, a reflective write would not
            return Shimmy(valueType, property::get, property::set)
        }
    }
}
