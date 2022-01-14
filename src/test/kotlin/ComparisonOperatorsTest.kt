import com.fasterxml.jackson.databind.node.BooleanNode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ComparisonOperatorsTest {

    @Test
    fun `test operator '=='`() {
        assertEquals(
                BooleanNode.TRUE,
                """{ "==" : [ 1, 1 ] } """.evaluateJson("{}")
        )

        assertEquals(
                BooleanNode.TRUE,
                """{ "==" : [ 1, "1" ] } """.evaluateJson("{}")
        )

        assertEquals(
                BooleanNode.FALSE,
                """{ "==" : [ 1, 2 ] } """.evaluateJson("{}")
        )
    }

    @Test
    fun `'in' should be able to handle nulls`() {

        val logic = """
                {
                    "in" : [ {
                        "var" : "x"
                        }, 
                        {
                            "var" : "y"
                        } 
                    ]
                }
                """

        assertEquals(BooleanNode.FALSE, logic.evaluateJson("""null"""))

        assertEquals(
                BooleanNode.FALSE,
                logic.evaluateJson(
                        """
                            {
                                "x" : null
                            }"""
                )
        )

        assertEquals(
                BooleanNode.FALSE,
                logic.evaluateJson(
                        """
                            {
                                "x" : null, 
                                "y" : null
                            } """
                )
        )
    }
}