import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import de.rki.jfn.JsonFunctions
import de.rki.jfn.error.NoSuchFunctionException
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonFunctionsTest {

    private val nodeFactory = JsonNodeFactory.instance

    @Test
    fun `registerFunction() should throw IllegalArgumentException on invalid input`() {
        JsonFunctions().run {
            assertThrows<IllegalArgumentException> {

                // empty object
                registerFunction("name", ObjectMapper().readTree("{}"))

                // no logic
                registerFunction("name", ObjectMapper().readTree("""{ "parameters": [] }"""))

                // no parameters
                registerFunction("name", ObjectMapper().readTree("""{ "logic": []]} }"""))

                // parameters as object instead of array
                registerFunction("name", ObjectMapper().readTree("""{ "parameters": {} }"""))

                // no parameters
                registerFunction("name", ObjectMapper().readTree("""{ "logic": [] }"""))
            }

            // correct: parameters as array, logic as object
            registerFunction(
                "name",
                ObjectMapper().readTree("""{ "parameters": [], "logic": [] }""")
            )
        }
    }

    @Test
    fun `evaluateFunction throws NoSuchFunctionException when no function was registered`() {
        JsonFunctions().run {
            assertThrows<NoSuchFunctionException> {
                evaluateFunction("unregisteredFunctionName", nodeFactory.objectNode())
            }
        }
    }

    @Test
    fun `determineData() should return empty node when function has no parameters`() {
        JsonFunctions().run {
            val parameters = ObjectMapper().readTree("[]") as ArrayNode
            val input = ObjectMapper().readTree("{}")
            assertEquals(
                ObjectMapper().readTree("{}"),
                determineData(parameters, input)
            )
        }
    }

    @Test
    fun `determineData() should return proper JsonNode`() {
        JsonFunctions().run {

            val parameters = ObjectMapper().readTree(
                """
                [
                    { 
                        "name": "string" 
                    },
                    { 
                        "name": "boolean"
                    }, 
                    { 
                        "name": "defaultLang", 
                        "default": "en"
                    }
                ]"""
            ) as ArrayNode

            val input = ObjectMapper().readTree(
                """
                { 
                    "string": "GREETING", 
                    "boolean": true
                }"""
            )

            val expectedData = ObjectMapper().readTree(
                """
                {
                    "string": "GREETING",
                    "boolean": true,
                    "defaultLang": "en"
                }"""
            )

            assertEquals(
                expectedData,
                determineData(parameters, input)
            )
        }
    }

    @Test
    fun `evaluate() test simple 'and' logic without parameters`() {
        JsonFunctions().run {

            val logic = ObjectMapper().readTree(
                """
                {
                    "<": [
                        1,
                        1
                    ]
                }"""
            )

            val result1 = evaluate(logic, nodeFactory.objectNode())
            assertEquals(BooleanNode.FALSE, result1)

            val logic2 = ObjectMapper().readTree(
                """
                {
                    "<": [
                        1,
                        2
                    ]
                }"""
            )

            val result2 = evaluate(logic2, nodeFactory.objectNode())
            assertEquals(BooleanNode.TRUE, result2)
        }
    }

    @Test
    fun `evaluate() test simple 'and' logic with parameters`() {
        JsonFunctions().run {

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
                }"""
            )

            val dataTrueFalse = ObjectMapper().readTree(
                """
                [
                    true,
                    false
                ]"""
            )

            val result1 = evaluate(logic, dataTrueFalse)
            assertEquals(BooleanNode.FALSE, result1)

            val dataTrueTrue = ObjectMapper().readTree(
                """
                [
                    true,
                    true
                ]"""
            )
            val result2 = evaluate(logic, dataTrueTrue)
            assertEquals(BooleanNode.TRUE, result2)
        }
    }

    @Test
    fun `evaluateFunction() test simple 'and' logic with parameters`() {
        JsonFunctions().run {

            registerFunction(
                "simpleAndLogic",
                ObjectMapper().readTree(
                    """
                {
                    "parameters": [
                        { 
                            "name": "firstValue" 
                        },
                        { 
                            "name": "secondValue" 
                        }
                    ],
                    "logic": [
                        {
                            "and": [
                                {
                                    "var": "firstValue"
                                },
                                {
                                    "var": "secondValue"
                                }
                            ]
                        }
                    ]
                }"""
                )
            )

            val dataTrueFalse = ObjectMapper().readTree(
                """
                    {
                        "firstValue": true,
                        "secondValue": false
                    }"""
            )
            val result1 = evaluateFunction("simpleAndLogic", dataTrueFalse)
            assertEquals(BooleanNode.FALSE, result1.first())

            val dataTrueTrue = ObjectMapper().readTree(
                """
                    {
                        "firstValue": true,
                        "secondValue": true
                    }"""
            )
            val result2 = evaluateFunction("simpleAndLogic", dataTrueTrue)
            assertEquals(BooleanNode.TRUE, result2.first())
        }
    }
}
