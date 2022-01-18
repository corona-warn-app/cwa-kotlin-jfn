package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.module.kotlin.contains

internal fun evaluateIf(
    arguments: ArrayNode,
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
    arguments: ArrayNode,
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
    arguments: ArrayNode,
    data: JsonNode
): JsonNode {
    val value = evaluateLogic(arguments[1], data)
    if (value.isArray) {
        throw IllegalArgumentException("Cannot initialize literal with object or array")
    }
    return value
}

// todo
internal fun evaluateObject(
    arguments: ArrayNode,
    data: JsonNode
): JsonNode {
//    let target = {}
//    for (let i = 1; i < values.length;) {
//        const value = values[i]
//        if (value.spread && Array.isArray(value.spread)) {
//            const obj = jfn.apply(value.spread[0], data)
//            if (typeof obj !== 'object') {
//                throw new Error('Spread for objects does not support non-objects')
//            }
//            target = {
//                ...target,
//                ...obj
//            }
//            i += 1
//        } else {
//            const property = jfn.apply(values[i], data)
//            if (typeof property === 'object') {
//                throw new Error('Key cannot be an object')
//            }
//            const value = jfn.apply(values[i + 1], data)
//            target[property] = value
//            i += 2
//        }
//    }
//    return target
    return NullNode.instance
}

internal fun evaluateArray(
    arguments: ArrayNode,
    data: JsonNode
): JsonNode {
    return ArrayNode(
        JsonNodeFactory.instance,
        arguments.mapIndexed { index, jsonNode ->
            if (jsonNode.isArray) {
                val arr = evaluateLogic(jsonNode[0], data)
                if (!arr.isArray) {
                    throw IllegalArgumentException("Spread for arrays only supports other arrays")
                }
                arr
            } else {
                evaluateLogic(jsonNode, data)
            }
        }
    )
}
