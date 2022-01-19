package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.contains
import de.rki.jfn.error.argError

internal fun evaluateIf(
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val ifCondition = if (arguments.contains(0)) arguments[0] else return NullNode.instance
    val conditionEvaluation = evaluateLogic(ifCondition, data)
    if (arguments.size() == 1) return conditionEvaluation

    if (isValueTruthy(conditionEvaluation)) {
        val thenStatement = if (arguments.contains(1)) arguments[1] else NullNode.instance
        return evaluateLogic(thenStatement, data)
    }
    if (isValueFalsy(conditionEvaluation)) {
        val elseStatement = if (arguments.contains(2)) arguments[2] else NullNode.instance
        return evaluateLogic(elseStatement, data)
    }
    throw IllegalArgumentException(
        "if-condition evaluates to something neither truthy, nor falsy: $conditionEvaluation"
    )
}

internal fun evaluateInit(
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val type = evaluateLogic(arguments[0], data).asText()
    if (type == "literal") {
        return evaluateLiteral(arguments, data)
    } else if (type == "object") {
        return evaluateObject(arguments, data)
    } else if (type == "array") {
        return evaluateArray(arguments, data)
    } else {
        throw IllegalArgumentException("Not supported type $type")
    }
}

internal fun evaluateLiteral(
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val value = evaluateLogic(arguments[1], data)
    if (value.isArray) {
        argError("Cannot initialize literal with object or array")
    }
    return value
}

internal fun evaluateObject(
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val target = ObjectNode(JsonNodeFactory.instance)
    var index = 1
    while (index < arguments.size()) {
        val jsonNode = arguments[index]
        if (jsonNode.has(SPREAD) && jsonNode.get(SPREAD).isArray) {
            val objectNode = evaluateLogic(jsonNode.get(SPREAD)[0], data)
            if (objectNode.isArray) {
                var i = 0
                objectNode.elements().forEach {
                    target.set<JsonNode>(i.toString(), it)
                    i ++
                }
            } else if (!objectNode.isNull) {
                objectNode as ObjectNode
                target.setAll<ObjectNode>(objectNode)
            }
            index += 1

        } else {
            val property = evaluateLogic(jsonNode, data)
            if (property.isObject || property.isArray || property.isNull)
                argError("Key must not be an object, array, or null.")
            val value = evaluateLogic(arguments[index + 1], data)
            target.replace(property.asText(), value)
            index += 2
        }
    }
    return target
}

internal fun evaluateArray(
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    return ArrayNode(
        JsonNodeFactory.instance,
        arguments.mapIndexedNotNull { index, jsonNode ->
            if (index == 0) return@mapIndexedNotNull null
            if (jsonNode.has(SPREAD) && jsonNode.get(SPREAD).isArray) {
                val arrayNode = evaluateLogic(jsonNode.get(SPREAD)[0], data)
                if (!arrayNode.isArray) {
                    throw IllegalArgumentException("Spread for arrays only supports other arrays")
                }
                arrayNode
            } else {
                evaluateLogic(jsonNode, data)
            }
        }
    )
}

private const val SPREAD = "spread"
