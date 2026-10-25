package at.hannibal2.skyhanni.test.utils

import at.hannibal2.skyhanni.utils.json.Shimmy
import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import io.github.notenoughupdates.moulconfig.observer.Property
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test


class ShimmyTest {

    enum class Stage { FIRST, SECOND }

    private class Simple { var value: String = "hello"; var number: Int = 42 }
    private class Nested { val inner = Simple() }
    private class DeepNested { val middle = Nested() }
    private class WithNullable { val inner: Simple? = null }
    private class WithProperty { val prop: Property<String> = Property.of("propertyValue") }
    private class WithIntProperty { val number: Property<Int> = Property.of(1) }
    private class WithEnumList { val list: MutableList<Stage> = mutableListOf() }
    private class WithEnumListProperty { val list: Property<MutableList<Stage>> = Property.of(mutableListOf()) }
    private class WithNullProperty { var prop: Property<Int>? = null }

    private fun shimmy(source: Any?, vararg path: String) = Shimmy(source, path.toList())

    private fun enumListJson() = JsonArray().apply { add("SECOND") }

    @Test fun `returns null for empty path`() = assertNull(shimmy(Simple()))
    @Test fun `returns null for null source`() = assertNull(shimmy(null, "value"))
    @Test fun `returns null for non-existent field`() = assertNull(shimmy(Simple(), "doesNotExist"))

    @Test fun `returns null when intermediate path segment is null`() =
        assertNull(shimmy(WithNullable(), "inner", "value"))

    @Test fun `reads single field`() =
        assertEquals(JsonPrimitive("hello"), shimmy(Simple(), "value")?.getJson())

    @Test fun `reads nested path`() =
        assertEquals(JsonPrimitive("hello"), shimmy(Nested(), "inner", "value")?.getJson())

    @Test fun `reads deeply nested path`() =
        assertEquals(JsonPrimitive(42), shimmy(DeepNested(), "middle", "inner", "number")?.getJson())

    @Test fun `sets single field`() {
        val source = Simple()
        shimmy(source, "value")?.setJson(JsonPrimitive("fromJson"))
        assertEquals("fromJson", source.value)
    }

    @Test fun `sets nested field`() {
        val source = Nested()
        shimmy(source, "inner", "number")?.setJson(JsonPrimitive(99))
        assertEquals(99, source.inner.number)
    }

    @Test fun `reads through moulconfig Property`() =
        assertEquals(JsonPrimitive("propertyValue"), shimmy(WithProperty(), "prop")?.getJson())

    @Test fun `sets through moulconfig Property`() {
        val source = WithProperty()
        shimmy(source, "prop")?.setJson(JsonPrimitive("newValue"))
        assertEquals("newValue", source.prop.get())
    }

    @Test fun `notifies Property observers`() {
        val source = WithProperty()
        var observed: String? = null
        source.prop.whenChanged { _, new -> observed = new }
        shimmy(source, "prop")?.setJson(JsonPrimitive("newValue"))
        assertEquals("newValue", observed)
    }

    @Test fun `keeps Int inside a Property`() {
        val source = WithIntProperty()
        shimmy(source, "number")?.setJson(JsonPrimitive(5))
        val number: Int = source.number.get()
        assertEquals(5, number)
    }

    @Test fun `keeps enum entries in a list`() {
        val source = WithEnumList()
        shimmy(source, "list")?.setJson(enumListJson())
        assertEquals(listOf(Stage.SECOND), source.list)
    }

    @Test fun `keeps enum entries in a list inside a Property`() {
        val source = WithEnumListProperty()
        shimmy(source, "list")?.setJson(enumListJson())
        assertEquals(listOf(Stage.SECOND), source.list.get())
    }

    @Test fun `creates a typed Property when the field holds none`() {
        val source = WithNullProperty()
        shimmy(source, "prop")?.setJson(JsonPrimitive(5))
        val number: Int? = source.prop?.get()
        assertEquals(5, number)
    }
}
