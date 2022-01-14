import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.IntNode
import de.rki.jfn.evaluateLogic
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class MathOperatorTest {

    private val objectMapper = ObjectMapper()

    @Test
    fun `happy addition`() {
        val logic = createLogic(rawLogic = """{ "+": [20,5] }""")

        evaluateLogic(logic, emptyNode).also {
            assertTrue { it is IntNode }
            assertEquals(it.intValue(), 25)
        }
    }

    @Test
    fun `happy subtraction`() {
        val logic = createLogic(rawLogic = """{ "-": [20,5] }""")

        evaluateLogic(logic, emptyNode).also {
            assertTrue { it is IntNode }
            assertEquals(it.intValue(), 15)
        }

        val logic2 = createLogic(rawLogic = """{ "-": [5,20] }""")

        evaluateLogic(logic2, emptyNode).also {
            assertTrue { it is IntNode }
            assertEquals(it.intValue(), (-15))
        }
    }

    @Test
    fun `happy division`() {
        val logic = createLogic(rawLogic = """{ "/": [20,5] }""")

        evaluateLogic(logic, emptyNode).also {
            assertTrue { it is IntNode }
            assertEquals(it.intValue(), 4)
        }

        val logic2 = createLogic(rawLogic = """{ "/": [5,20] }""")

        evaluateLogic(logic2, emptyNode).also {
            assertTrue { it is IntNode }
            assertEquals(it.intValue(), 0)
        }
    }

    @Test
    fun `happy multiplication`() {
        val logic = createLogic(rawLogic = """{ "*": [20,5] }""")

        evaluateLogic(logic, emptyNode).also {
            assertTrue { it is IntNode }
            assertEquals(it.intValue(), 100)
        }
    }

    @Test
    fun `happy modulo`() {
        val logic = createLogic(rawLogic = """{ "%": [21,5] }""")

        evaluateLogic(logic, emptyNode).also {
            assertTrue { it is IntNode }
            assertEquals(it.intValue(), 1)
        }
    }

    @Test
    fun `throws runtime exception if logic is faulty`() {
        var logic = createLogic(rawLogic = """{ "*": [20,"5"] }""")
        assertThrows<IllegalArgumentException> { evaluateLogic(logic, emptyNode) }

        logic = createLogic(rawLogic = """{ "*": ["20","5"] }""")
        assertThrows<IllegalArgumentException> { evaluateLogic(logic, emptyNode) }

        logic = createLogic(rawLogic = """{ "%": [20] }""")
        assertThrows<IllegalArgumentException> { evaluateLogic(logic, emptyNode) }
    }

    private fun createLogic(rawLogic: String) = objectMapper.readTree(rawLogic)

    private val emptyNode: JsonNode get() = objectMapper.createObjectNode()
}
