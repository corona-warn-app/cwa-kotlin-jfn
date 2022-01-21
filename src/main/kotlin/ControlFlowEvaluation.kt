package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.contains
import de.rki.jfn.error.argError

internal fun evaluateCall(
    jfn: JsonFunctions,
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val name = arguments[0]
    if (!name.isTextual) argError("Function name must be a string")

    val parameters = if (arguments.has(1) && !arguments[1].isNull) arguments[1]
    else JsonNodeFactory.instance.objectNode()

    if (!parameters.isObject) argError("Parameters must be an object")

    val functionDescriptor = jfn.getDescriptor(name.asText())
    val functionDescriptorParameters =
        functionDescriptor.get("parameters") as ArrayNode
    val functionDescriptorLogic = functionDescriptor.get("logic") as ArrayNode

    val scopedData = JsonNodeFactory.instance.objectNode().apply {
        functionDescriptorParameters.forEach {
            val propertyName = it.get("name").textValue()
            when {
                parameters.has(propertyName) -> set<JsonNode>(
                    propertyName,
                    evaluateLogic(jfn, parameters[propertyName], data)
                )
                it.has("default") -> set<JsonNode>(propertyName, it["default"])
                else -> set<JsonNode>(propertyName, NullNode.instance)
            }
        }
    }
    return jfn.evaluate(functionDescriptorLogic, scopedData)
}

internal fun evaluateAssign(
    jfn: JsonFunctions,
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val identifier = evaluateLogic(jfn, arguments[0], data)
    val value = evaluateLogic(jfn, arguments[1], data)

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
    jfn: JsonFunctions,
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val identifier = evaluateLogic(jfn, arguments[0], data)
    if (!identifier.isTextual) argError("First parameter of declare must be a string")

    val value = evaluateLogic(jfn, arguments[1], data)
    data as ObjectNode
    data.replace(identifier.asText(), value)
    return NullNode.instance
}

internal fun evaluateScript(
    jfn: JsonFunctions,
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val scopedData = JsonNodeFactory.instance.objectNode().setAll<JsonNode>(data as ObjectNode)
    try {
        arguments.forEach { evaluateLogic(jfn, it, scopedData) }
    } catch (e: ReturnException) {
        return e.data
    }
    return NullNode.instance
}

internal fun evaluateIf(
    jfn: JsonFunctions,
    arguments: JsonNode,
    data: JsonNode
): JsonNode {

    var index = 0
    while (index < arguments.size()) {
        val conditionEvaluation = evaluateLogic(jfn, arguments[index], data)
        if (isValueTruthy(conditionEvaluation)) // if condition met or else branch
            return if (arguments.contains(index + 1))
                evaluateLogic(jfn, arguments[index + 1], data) // if condition met
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
    jfn: JsonFunctions,
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val ifCondition = if (arguments.contains(0)) arguments[0] else return NullNode.instance
    val conditionEvaluation = evaluateLogic(jfn, ifCondition, data)
    if (arguments.size() == 1) return conditionEvaluation

    if (isValueTruthy(conditionEvaluation)) {
        val thenStatement = if (arguments.contains(1)) arguments[1] else NullNode.instance
        return evaluateLogic(jfn, thenStatement, data)
    }
    if (isValueFalsy(conditionEvaluation)) {
        val elseStatement = if (arguments.contains(2)) arguments[2] else NullNode.instance
        return evaluateLogic(jfn, elseStatement, data)
    }
    argError(
        "if-condition evaluates to something neither truthy, nor falsy: $conditionEvaluation"
    )
}

internal fun evaluateInit(
    jfn: JsonFunctions,
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    return when (val type = evaluateLogic(jfn, arguments[0], data).asText()) {
        "literal" -> evaluateLiteral(jfn, arguments, data)
        "object" -> evaluateObject(jfn, arguments, data)
        "array" -> evaluateArray(jfn, arguments, data)
        else -> argError("Not supported type $type")
    }
}

internal fun evaluateLiteral(
    jfn: JsonFunctions,
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val value = evaluateLogic(jfn, arguments[1], data)
    if (value.isArray) {
        argError("Cannot initialize literal with object or array")
    }
    return value
}

internal fun evaluateObject(
    jfn: JsonFunctions,
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val target = ObjectNode(JsonNodeFactory.instance)
    var index = 1
    while (index < arguments.size()) {
        val jsonNode = arguments[index]
        if (jsonNode.has(SPREAD) && jsonNode.get(SPREAD).isArray) {
            val objectNode = evaluateLogic(jfn, jsonNode.get(SPREAD)[0], data)
            if (objectNode.isArray) {
                var i = 0
                objectNode.elements().forEach {
                    target.set<JsonNode>(i.toString(), it)
                    i++
                }
            } else if (!objectNode.isNull) {
                objectNode as ObjectNode
                target.setAll<ObjectNode>(objectNode)
            }
            index += 1
        } else {
            val property = evaluateLogic(jfn, jsonNode, data)
            if (property.isObject || property.isArray || property.isNull)
                argError("Key must not be an object, array, or null.")
            val value = evaluateLogic(jfn, arguments[index + 1], data)
            target.replace(property.asText(), value)
            index += 2
        }
    }
    return target
}

internal fun evaluateArray(
    jfn: JsonFunctions,
    arguments: JsonNode,
    data: JsonNode
): JsonNode {
    val list = mutableListOf<JsonNode>()
    arguments.filterIndexed { index, _ ->
        index != 0
    }.forEach { jsonNode ->
        if (jsonNode.has(SPREAD) && jsonNode.get(SPREAD).isArray) {
            val arrayNode = evaluateLogic(jfn, jsonNode.get(SPREAD)[0], data)
            if (!arrayNode.isArray) {
                argError("Spread for arrays only supports other arrays")
            }

            arrayNode.elements().forEach {
                list.add(it)
            }
        } else {
            list.add(evaluateLogic(jfn, jsonNode, data))
        }
    }
    return JsonNodeFactory.instance.arrayNode().addAll(list)
}

internal fun evaluateVar(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {

    val path = when {
        args.isArray && args.isEmpty -> return data
        args.isArray && args.first().isObject -> {
            // argument is an operation, so let's evaluate it
            evaluateLogic(jfn, args.first(), data).asText()
        }
        args.isArray && args.size() == 1 -> args.first().asText()
        args.isArray && args.size() > 1 -> {
            // return last element of array if the argument is an array with more than 1 element
            return args.last()
        }
        args.isNull || args.asText() == "" -> return data
        else -> args.asText()
    }

    return path.split(".").fold(data) { acc, fragment ->
        if (acc is NullNode) {
            acc
        } else {
            try {
                val index = fragment.toInt()
                if (acc is ArrayNode) acc[index] else null
            } catch (e: NumberFormatException) {
                if (acc is ObjectNode) acc[fragment] else null
            } ?: NullNode.instance
        }
    }
}

internal class ReturnException(val data: JsonNode) : Exception()

private const val SPREAD = "spread"
