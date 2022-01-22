import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ValueNode
import de.rki.jfn.JsonFunctions

val objectMapper = ObjectMapper()
val engine = JsonFunctions()

fun String.toJsonNode(): JsonNode = objectMapper.readTree(this)

fun String.evaluateJson(data: String): JsonNode = engine.evaluate(
    this.toJsonNode(),
    data.toJsonNode()
)

fun String.evaluateJson(data: ValueNode): JsonNode = engine.evaluate(
    this.toJsonNode(),
    data
)
