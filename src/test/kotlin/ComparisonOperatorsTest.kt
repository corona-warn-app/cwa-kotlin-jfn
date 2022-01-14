
import com.fasterxml.jackson.databind.node.BooleanNode
import de.rki.jfn.evaluateLogic
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ComparisonOperatorsTest {

    @Test
    fun `'in' should be able to handle nulls`() {

        val logic = """
            {
                "in" : [ {
                        "var" : "x"
                    }, {
                        "var" : "y"
                    } 
                ]
            }
        """.toJsonNode()

        val nullData = """null""".toJsonNode()
        assertEquals(BooleanNode.FALSE, evaluateLogic(logic, nullData))

        val emptyObjectData = """{ }""".toJsonNode()
        assertEquals(BooleanNode.FALSE, evaluateLogic(logic, emptyObjectData))

        val xIsNullData = """
            {
                "x" : null
            }
        """.toJsonNode()
        assertEquals(BooleanNode.FALSE, evaluateLogic(logic, xIsNullData))

        val xAndYAreNullData = """
            {
                "x" : null, 
                "y" : null
            }
        """.toJsonNode()
        assertEquals(BooleanNode.FALSE, evaluateLogic(logic, xAndYAreNullData))
    }
}