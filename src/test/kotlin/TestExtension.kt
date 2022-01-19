import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ValueNode
import de.rki.jfn.evaluateLogic

val objectMapper = ObjectMapper()

fun String.toJsonNode(): JsonNode = objectMapper.readTree(this)

fun String.evaluateJson(data: String): JsonNode = evaluateLogic(
    this.toJsonNode(),
    data.toJsonNode()
)

fun String.evaluateJson(data: ValueNode): JsonNode = evaluateLogic(
    this.toJsonNode(),
    data
)
