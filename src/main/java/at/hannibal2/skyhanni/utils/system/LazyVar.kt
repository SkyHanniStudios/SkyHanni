package at.hannibal2.skyhanni.utils.system

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Allows lazy initialization of a variable that can be set later.
 * Usage example;
 * var x: Int by LazyVar<Int> { lateLoadCallGetInt() }
 */
class LazyVar<T>(val initializer: () -> T) : ReadWriteProperty<Any?, T> {
    private var value: T? = null
    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        value ?: initializer().also { value = it }
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this@LazyVar.value = value
    }
}
