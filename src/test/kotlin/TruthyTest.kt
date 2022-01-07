import com.fasterxml.jackson.databind.node.*
import de.rki.jfn.JsonFunctionsEngine
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TruthyTest {

    @Test
    fun `test isTruthy()`() {
        // TODO: add attribution for certlogic - this code was copied from it
        JsonFunctionsEngine().run {
            assertFalse(isTruthy(NullNode.instance))
            assertFalse(isTruthy(BooleanNode.FALSE))
            assertTrue(isTruthy(BooleanNode.TRUE))
            assertFalse(isTruthy(JsonNodeFactory.instance.arrayNode()), "empty array")
            assertTrue(
                isTruthy(JsonNodeFactory.instance.arrayNode().add(TextNode.valueOf("foo"))),
                "non-empty array"
            )
            assertFalse(isTruthy(JsonNodeFactory.instance.objectNode()), "empty object")
            assertTrue(isTruthy(JsonNodeFactory.instance.objectNode().put("foo", "bar")), "non-empty object")
            assertTrue(isTruthy(TextNode.valueOf("foo")))
            assertFalse(isTruthy(TextNode.valueOf("")))
            assertTrue(isTruthy(IntNode.valueOf(42)))
            assertFalse(isTruthy(IntNode.valueOf(0)))
        }
    }

}