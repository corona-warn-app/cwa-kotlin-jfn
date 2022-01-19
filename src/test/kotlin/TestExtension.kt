import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ValueNode
import de.rki.jfn.JsonFunctionsEngine
import de.rki.jfn.evaluateLogic

val objectMapper = ObjectMapper()
val engine = JsonFunctionsEngine()

fun String.toJsonNode(): JsonNode = objectMapper.readTree(this)

fun String.evaluateJson(data: String): JsonNode = engine.evaluate(
    this.toJsonNode(),
    data.toJsonNode()
)

fun String.evaluateJson(data: ValueNode): JsonNode = engine.evaluate(
    this.toJsonNode(),
    data
)
