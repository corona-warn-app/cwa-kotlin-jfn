package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.contains
import de.rki.jfn.error.argError

internal fun evaluateAssign(
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val identifier = evaluateLogic(arguments[0], data)
    val value = evaluateLogic(arguments[1], data)

    if (!identifier.isTextual) argError("First parameter of assign must be a string")

    val identifierChunks = identifier.asText().split('.').toMutableList()
    val propertyName = identifierChunks.last()
    identifierChunks.removeLast()

    val newData = identifierChunks.fold(data) { acc, chunk ->
        if (acc.isArray) acc[Integer.valueOf(chunk)] else acc.get(chunk)
    }
    if (newData.isArray) {
        newData as ArrayNode
        val index = Integer.valueOf(propertyName).toInt()
        if (index < newData.size()) newData.set(index, value)
        else newData.add(value)
    } else (newData as ObjectNode).replace(propertyName, value)

    return NullNode.instance
}

internal fun evaluateDeclare(
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val identifier = evaluateLogic(arguments[0], data)
    if (!identifier.isTextual) argError("First parameter of declare must be a string")

    val value = evaluateLogic(arguments[1], data)
    data as ObjectNode
    data.replace(identifier.asText(), value)
    return NullNode.instance
}

internal fun evaluateScript(
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val scopedData = JsonNodeFactory.instance.objectNode().setAll<JsonNode>(data as ObjectNode)
    try {
        arguments.forEach { evaluateLogic(it, scopedData) }
    } catch (e: ReturnException) {
        return e.data
    }
    return NullNode.instance
}

internal fun evaluateIf(
    arguments: JsonNode,
    data: JsonNode
): JsonNode {

    var index = 0
    while (index < arguments.size()) {
        val conditionEvaluation = evaluateLogic(arguments[index], data)
        if (isValueTruthy(conditionEvaluation)) // if condition met or else branch
            return if (arguments.contains(index + 1))
                evaluateLogic(arguments[index + 1], data) // if condition met
            else conditionEvaluation // else branch

        if (index + 2 >= arguments.size()) { // no further else if
            return if (isValueFalsy(conditionEvaluation) && arguments.contains(index + 1))
                NullNode.instance // else-if condition not met
            else conditionEvaluation // else branch
        }
        index += 2
    }

    return NullNode.instance
}

internal fun evaluateTernary(
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
    argError(
        "if-condition evaluates to something neither truthy, nor falsy: $conditionEvaluation"
    )
}

internal fun evaluateInit(
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val type = evaluateLogic(arguments[0], data).asText()
    return when (type) {
        "literal" -> evaluateLiteral(arguments, data)
        "object" -> evaluateObject(arguments, data)
        "array" -> evaluateArray(arguments, data)
        else -> argError("Not supported type $type")
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
    val list = mutableListOf<JsonNode>()
    arguments.filterIndexed { index, _ ->
        index != 0
    }.forEach { jsonNode ->
        if (jsonNode.has(SPREAD) && jsonNode.get(SPREAD).isArray) {
            val arrayNode = evaluateLogic(jsonNode.get(SPREAD)[0], data)
            if (!arrayNode.isArray) {
                argError("Spread for arrays only supports other arrays")
            }

            arrayNode.elements().forEach {
                list.add(it)
            }
        } else {
            list.add(evaluateLogic(jsonNode, data))
        }
    }
    return JsonNodeFactory.instance.arrayNode().addAll(list)
}

internal class ReturnException(
    val data: JsonNode
) : Exception()

private const val SPREAD = "spread"
