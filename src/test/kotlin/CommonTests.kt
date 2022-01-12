import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.JsonFunctionsEngine
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import kotlin.test.assertEquals

class CommonTests {

    @ParameterizedTest(name = "{index}: {0}")
    @ArgumentsSource(CommonTestCaseProvider::class)
    fun `execute all tests from specification`(testCase: JsonNode) {

        val engine = JsonFunctionsEngine()

        if (!testCase.has("title") || testCase["title"] !is TextNode) {
            println("Invalid testcase - no title defined or title is not a string")
            return
        }

        println(" Executing TestCase: ${testCase.toPrettyString()}")

        if (testCase.has("functions")) {
            testCase["functions"].forEach {
                val name = it["name"] ?: run {
                    println("Invalid testcase - property 'name' missing")
                    return
                }
                if (name !is TextNode) {
                    println("Invalid testcase - value of property 'name' is not a string")
                    return
                }
                val definition = it["definition"] ?: run {
                    println("Invalid testcase - property 'definition' missing")
                    return
                }

                engine.registerFunction(name.textValue(), definition)
            }
        }

        if (testCase.has("evaluateFunction")) {
            val ef = testCase["evaluateFunction"]
            val name = ef["name"] ?: run {
                println("Invalid testcase - property 'name' missing")
                return
            }
            if (name !is TextNode) {
                println("Invalid testcase - value of property 'name' is not a string")
                return
            }
            val parameters = ef["parameters"] ?: run {
                println("Invalid testcase - property 'parameters' missing")
                return
            }

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
                    println("Invalid testcase - no property with name 'exp' or 'throws' found")
                }
            }
        }

        if (testCase.has("logic") && testCase.has("data")) {

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
                    println("Invalid testcase - no property with name 'exp' or 'throws' found")
                }
            }
        }
    }
}