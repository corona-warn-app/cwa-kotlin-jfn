import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import de.rki.jfn.JsonFunctionsEngine
import de.rki.jfn.NoSuchFunctionException
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonFunctionsTest {

    private val nodeFactory = JsonNodeFactory.instance

    @Test
    fun `evaluateFunction() should throw NoSuchFunctionException() when no function was registered before`() {
        JsonFunctionsEngine().run {
            assertThrows<NoSuchFunctionException> {
                evaluateFunction("unregisteredFunctionName", nodeFactory.nullNode())
            }
        }
    }

    @Test
    fun `determineData() should return proper JsonNode`() {
        JsonFunctionsEngine().run {

            val parameters = ObjectMapper().readTree(
                """[
                        { 
                            "name": "key" 
                        },
                        { 
                            "name": "lang" 
                        }, 
                        { 
                            "name": "defaultLang", 
                            "default": "en"
                        }
                    ]
                }"""
            )

            val input = ObjectMapper().readTree(
                """
                { 
                    "key": "GREETING", 
                    "lang": "de"
                }"""
            )

            val expectedData = ObjectMapper().readTree(
                """
                {
                    "key": "GREETING",
                    "lang": "de",
                    "defaultLang": "en"
                }"""
            )

            assertEquals(
                expectedData,
                determineData(parameters as ArrayNode, input)
            )
        }
    }

    @Test
    fun `eval() test simple 'and' logic`() {
        JsonFunctionsEngine().run {

            val logic = ObjectMapper().readTree(
                """
            {
                "and": [
                {
                    "var": "0"
                },
                {
                    "var": "1"
                }
                ]
            }
            """
            )

            val dataTrueFalse = ObjectMapper().readTree(
                """
            [
                true,
                false
            ]
            """
            )
            val result1 = evaluate(logic, dataTrueFalse)
            assertEquals(BooleanNode.FALSE, result1)

            val dataTrueTrue = ObjectMapper().readTree(
                """
            [
                true,
                true
            ]
            """
            )
            val result2 = evaluate(logic, dataTrueTrue)
            assertEquals(BooleanNode.TRUE, result2)
        }
    }
}
