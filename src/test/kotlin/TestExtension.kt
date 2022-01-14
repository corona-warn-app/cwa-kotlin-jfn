import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

val objectMapper = ObjectMapper()

fun String.toJsonNode(): JsonNode = objectMapper.readTree(this)