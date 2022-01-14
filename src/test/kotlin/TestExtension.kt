import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.jfn.evaluateLogic

val objectMapper = ObjectMapper()

fun String.toJsonNode(): JsonNode = objectMapper.readTree(this)

fun String.evaluateJson(data: String) = evaluateLogic(this.toJsonNode(), data.toJsonNode())