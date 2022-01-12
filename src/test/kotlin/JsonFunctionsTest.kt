import com.fasterxml.jackson.databind.JsonNode
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
    fun `registerFunction() should throw RuntimeException() on invalid input`() {
        JsonFunctionsEngine().run {
            assertThrows<RuntimeException> {

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
            registerFunction("name", ObjectMapper().readTree("""{ "parameters": [], "logic": [] }"""))
        }
    }

    @Test
    fun `evaluateFunction() should throw NoSuchFunctionException() when no function was registered before`() {
        JsonFunctionsEngine().run {
            assertThrows<NoSuchFunctionException> {
                evaluateFunction("unregisteredFunctionName", nodeFactory.objectNode())
            }
        }
    }

    @Test
    fun `determineData() should return empty node when function has no parameters`() = JsonFunctionsEngine().run {
        val parameters = ObjectMapper().readTree("[]") as ArrayNode
        val input = ObjectMapper().readTree("{}")
        assertEquals(
            ObjectMapper().readTree("{}"),
            determineData(parameters, input)
        )
    }

    @Test
    fun `determineData() should return proper JsonNode`() {
        JsonFunctionsEngine().run {

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

            // "boolean" missing
            val invalidInput = ObjectMapper().readTree(
                """
                { 
                    "string": "GREETING"
                }"""
            )
            assertThrows<RuntimeException> {
                determineData(parameters, invalidInput)
            }
        }
    }

    @Test
    fun `eval() test simple 'and' logic without parameters`() {
        JsonFunctionsEngine().run {

            val logic = ObjectMapper().readTree(
                """
            [
                {
                    "<": [
                        1,
                        1
                    ]
                }
            ]"""
            )

            val result1 = evaluate(logic.first(), nodeFactory.objectNode())
            assertEquals(BooleanNode.FALSE, result1)

            val logic2 = ObjectMapper().readTree(
                """
            [
                {
                    "<": [
                        1,
                        2
                    ]
                }
            ]"""
            )

            val result2 = evaluate(logic2.first(), nodeFactory.objectNode())
            assertEquals(BooleanNode.TRUE, result2)
        }
    }

    @Test
    fun `eval() test simple 'and' logic with parameters`() {
        JsonFunctionsEngine().run {

            val logic = ObjectMapper().readTree(
                """
            [
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
            ]"""
            )

            val dataTrueFalse = ObjectMapper().readTree(
                """
                [
                    true,
                    false
                ]"""
            )

            val result1 = evaluate(logic.first(), dataTrueFalse)
            assertEquals(BooleanNode.FALSE, result1)

            val dataTrueTrue = ObjectMapper().readTree(
                """
                [
                    true,
                    true
                ]"""
            )
            val result2 = evaluate(logic.first(), dataTrueTrue)
            assertEquals(BooleanNode.TRUE, result2)
        }
    }

    @Test
    fun `evaluateFunction() test simple 'and' logic with parameters`() {
        JsonFunctionsEngine().run {

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
