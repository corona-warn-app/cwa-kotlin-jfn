import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.JsonFunctionsEngine
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import kotlin.test.assertEquals
import kotlin.test.fail

class CommonTests {

    @ParameterizedTest(name = "{index}: {0}")
    @ArgumentsSource(CommonTestCaseProvider::class)
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    fun `execute all tests from specification`(testCase: JsonNode) {

        val engine = JsonFunctionsEngine()

        println("Executing TestCase: ${testCase.toPrettyString()}")

        when {
            testCase.has("functions") -> {
                testCase["functions"].forEach {
                    registerFunction(it, engine)
                }
                evaluateFunction(testCase, engine)
            }

            testCase.has("logic") && testCase.has("data") -> {
                evaluate(testCase, engine)
            }

            else -> fail("Test case has wrong format")
        }
    }

    private fun evaluateFunction(testCase: JsonNode, engine: JsonFunctionsEngine) {
        val function = testCase["evaluateFunction"]
        val name = function["name"] ?: fail("Invalid testcase - property 'name' missing")
        if (name !is TextNode) {
            fail("Invalid testcase - value of property 'name' is not a string")
        }
        val parameters =
            function["parameters"] ?: fail("Invalid testcase - property 'parameters' missing")

        when {
            testCase.has("throws") -> {
                assertThrows<Exception> {
                    engine.evaluateFunction(name.textValue(), parameters)
                }
            }
            testCase.has("exp") -> {
                assertEquals(
                    testCase["exp"],
                    engine.evaluateFunction(name.textValue(), parameters)
                )
            }
            else -> {
                fail("Invalid testcase - no property with name 'exp' or 'throws' found")
            }
        }
    }

    private fun registerFunction(it: JsonNode, engine: JsonFunctionsEngine) {
        val name = it["name"] ?: fail("Invalid testcase - property 'name' missing")
        if (name !is TextNode) {
            fail("Invalid testcase - value of property 'name' is not a string")
        }
        val definition =
            it["definition"] ?: fail("Invalid testcase - property 'definition' missing")

        engine.registerFunction(name.textValue(), definition)
    }

    private fun evaluate(testCase: JsonNode, engine: JsonFunctionsEngine) {
        val logic = testCase["logic"]
        val data = testCase["data"]

        when {
            testCase.has("throws") -> {
                assertThrows<Exception> {
                    engine.evaluate(logic, data)
                }
            }
            testCase.has("exp") -> {
                assertEquals(
                    testCase["exp"],
                    engine.evaluate(logic, data)
                )
            }
            else -> {
                fail("Invalid testcase - no property with name 'exp' or 'throws' found")
            }
        }
    }
}
