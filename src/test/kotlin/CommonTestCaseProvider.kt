import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.io.File
import java.util.stream.Stream

class CommonTestCaseProvider : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val testsFile = File("src/test/resources/jfn-common-test-cases.gen.json")
        val tree = ObjectMapper().readTree(testsFile)
        val testCases = tree.get("testCases") as ArrayNode
        return testCases
            // for single test execution
            // .filter { it.get("title").textValue().startsWith("test case 145") }
            .map { Arguments.of(Named.of(it.get("title").textValue(), it)) }.stream()
    }
}