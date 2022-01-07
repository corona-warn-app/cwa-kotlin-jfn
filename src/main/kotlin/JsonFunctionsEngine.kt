package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*

class JsonFunctionsEngine : JsonFunctions {

    override fun registerFunction(name: String, descriptor: JsonNode) {
        TODO("Not yet implemented")
    }

    override fun evaluateFunction(name: String, parameters: JsonNode): Any? {
        TODO("Not yet implemented")
    }

    // same implementation as certlogic
    override fun isTruthy(value: JsonNode) = when (value) {
        is BooleanNode -> value == BooleanNode.TRUE
        is TextNode -> value.textValue().isNotEmpty()
        is IntNode -> value.intValue() != 0
        is ArrayNode -> value.size() > 0
        is ObjectNode -> value.size() > 0
        else -> false
    }

    override fun evaluate(logic: JsonNode, data: JsonNode): JsonNode {
        TODO("Not yet implemented")
    }
}