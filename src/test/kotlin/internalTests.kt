import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.extractFromUVCI
import de.rki.jfn.isValueFalsy
import de.rki.jfn.isValueTruthy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Should be in sync with the `describe("truthy and falsy", ...`-part of `test-internals.ts` from `certlogic-js`.
 */
internal class TruthyFalsyTests {

    @Test
    fun `test isValueTruthy`() {
        // (no undefined)
        assertFalse(isValueTruthy(NullNode.instance))
        assertFalse(isValueTruthy(BooleanNode.FALSE))
        assertTrue(isValueTruthy(BooleanNode.TRUE))
        assertFalse(isValueTruthy(JsonNodeFactory.instance.arrayNode()), "empty array")
        assertTrue(
            isValueTruthy(JsonNodeFactory.instance.arrayNode().add(TextNode.valueOf("foo"))),
            "non-empty array"
        )
        assertFalse(isValueTruthy(JsonNodeFactory.instance.objectNode()), "empty object")
        assertTrue(isValueTruthy(JsonNodeFactory.instance.objectNode().put("foo", "bar")), "non-empty object")
        assertTrue(isValueTruthy(TextNode.valueOf("foo")))
        assertFalse(isValueTruthy(TextNode.valueOf("")))
        assertTrue(isValueTruthy(IntNode.valueOf(42)))
        assertFalse(isValueTruthy(IntNode.valueOf(0)))
    }

    @Test
    fun `test isValueFalsy`() {
        // (no undefined)
        assertTrue(isValueFalsy(NullNode.instance))
        assertTrue(isValueFalsy(BooleanNode.FALSE))
        assertFalse(isValueFalsy(BooleanNode.TRUE))
        assertTrue(isValueFalsy(JsonNodeFactory.instance.arrayNode()), "empty array")
        assertFalse(
            isValueFalsy(JsonNodeFactory.instance.arrayNode().add(TextNode.valueOf("foo"))),
            "non-empty array"
        )
        assertTrue(isValueFalsy(JsonNodeFactory.instance.objectNode()), "empty object")
        assertTrue(isValueTruthy(JsonNodeFactory.instance.objectNode().put("foo", "bar")), "non-empty object")
        assertFalse(isValueFalsy(TextNode.valueOf("foo")))
        assertTrue(isValueFalsy(TextNode.valueOf("")))
        assertFalse(isValueFalsy(IntNode.valueOf(42)))
        assertTrue(isValueFalsy(IntNode.valueOf(0)))
    }

}

/**
 * Should be in sync with the `describe("extractFromUVCI", ...`-part of `test-internals.ts` from `certlogic-js`.
 */
internal class ExtractFromUVCITests {

    internal fun checkForThat(uvci: String?, assertions: List<Pair<Int, String?>>): Unit =
        assertions.forEach {
            Assertions.assertEquals(it.second, extractFromUVCI(uvci, it.first))
        }

    @Test
    fun `returns null on null operand`() {
        checkForThat(
            null,
            listOf(
                -1 to null,
                0 to null,
                1 to null
            )
        )
    }

    @Test
    fun `works correctly on an empty string`() {
        checkForThat(
            "",
            listOf(
                -1 to null,
                0 to "",
                1 to null
            )
        )
    }

    @Test
    fun `that thing that testers do (without optional prefix)`() {
        checkForThat(
            "foo/bar::baz#999lizards",
            listOf(
                -1 to null,
                0 to "foo",
                1 to "bar",
                2 to "",    // not null, but still falsy
                3 to "baz",
                4 to "999lizards",
                5 to null
            )
        )
    }

    @Test
    fun `that thing that testers do (with optional prefix)`() {
        checkForThat(
            "URN:UVCI:foo/bar::baz#999lizards",
            listOf(
                -1 to null,
                0 to "foo",
                1 to "bar",
                2 to "",    // not null, but still falsy
                3 to "baz",
                4 to "999lizards",
                5 to null
            )
        )
    }

    // the example from the specification:
    @Test
    fun `each separator adds a fragment`() {
        checkForThat(
            "a::c/#/f",
            listOf(
                0 to "a",
                1 to "",
                2 to "c",
                3 to "",
                4 to "",
                5 to "f"
            )
        )
    }

}