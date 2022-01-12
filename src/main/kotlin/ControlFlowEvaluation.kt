package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode

internal fun evaluateIf(
        arguments: ArrayNode,
        data: JsonNode
): JsonNode {
    if (arguments.size() != 3) {
        throw IllegalArgumentException("There must be exactly 3 arguments.")
    }
    val ifCondition: JsonNode = arguments[0]
    val thenStatement: JsonNode = arguments[1]
    val elseStatement: JsonNode = arguments[2]

    val conditionEvaluation = evaluateLogic(ifCondition, data)
    if (isValueTruthy(conditionEvaluation)) {
        return evaluateLogic(thenStatement, data)
    }
    if (isValueFalsy(conditionEvaluation)) {
        return evaluateLogic(elseStatement, data)
    }
    throw IllegalArgumentException(
            "if-condition evaluates to something neither truthy, nor falsy: $conditionEvaluation"
    )
}
