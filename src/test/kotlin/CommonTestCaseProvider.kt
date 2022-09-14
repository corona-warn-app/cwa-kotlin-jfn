import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class CommonTestCaseProvider : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val testsFile = javaClass.classLoader.getResource("jfn-common-test-cases.gen.json")
        val tree = ObjectMapper().readTree(testsFile)
        val testCases = tree.get("testCases") as ArrayNode
        val commonFunctions = tree.get("commonFunctions")

        return testCases.map { case ->
            val newCase = if (commonFunctions != null) {
                (case as ObjectNode).set<ArrayNode>("functions", commonFunctions)
            } else {
                case
            }
            Arguments.of(Named.of(newCase.get("title").textValue(), newCase))
        }.stream()
    }
}
