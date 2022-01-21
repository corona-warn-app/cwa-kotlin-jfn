/*
    Copied from:
    https://github.com/ehn-dcc-development/dgc-business-rules/blob/main/certlogic/certlogic-kotlin/src/main/kotlin/eu/ehn/dcc/certlogic/certlogic.kt

    Modifications Copyright (c) 2022 SAP SE or an SAP affiliate company.
*/

package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.error.argError
import de.rki.jfn.operators.AccessingDataOperator
import de.rki.jfn.operators.ArrayOperator
import de.rki.jfn.operators.ComparisonOperator
import de.rki.jfn.operators.ControlFlowOperator
import de.rki.jfn.operators.MathOperator
import de.rki.jfn.operators.StringOperator
import de.rki.jfn.operators.TimeOperator

fun evaluateLogic(
    jfn: JsonFunctions,
    logic: JsonNode,
    data: JsonNode
): JsonNode = when (logic) {
    is TextNode -> logic
    is NumericNode -> logic
    is BooleanNode -> logic
    is NullNode -> logic
    is ArrayNode -> {
        JsonNodeFactory.instance.arrayNode().addAll(logic.map { evaluateLogic(jfn, it, data) })
    }
    is ObjectNode -> {
        if (logic.size() != 1) {
            logic
        } else {
            val operators = ArrayOperator +
                StringOperator +
                TimeOperator +
                MathOperator +
                AccessingDataOperator +
                ComparisonOperator +
                ControlFlowOperator

            val (operator, args) = logic.fields().next()
            when (operator) {
                "!" -> evaluateNot(jfn, args, data)
                else -> {
                    when (operator) {
                        in operators -> operators(operator, jfn, args, data)
                        "extractFromUVCI" -> evaluateExtractFromUVCI(jfn, args[0], args[1], data)
                        else -> argError("unrecognised operator: $operator")
                    }
                }
            }
        }
    }
    else -> throw RuntimeException("invalid JsonFunctions expression: ${logic.toPrettyString()}")
}

internal fun evaluateNot(
    jfn: JsonFunctions,
    operandExpr: JsonNode,
    data: JsonNode
): JsonNode {

    val operand = if (operandExpr.isArray) {
        evaluateLogic(jfn, operandExpr, data)[0]
    } else {
        operandExpr
    }
    if (isValueFalsy(operand)) {
        return BooleanNode.TRUE
    }
    if (isValueTruthy(operand)) {
        return BooleanNode.FALSE
    }
    throw RuntimeException(
        "operand of ! evaluates to something neither truthy, nor falsy: $operand"
    )
}

internal fun evaluateExtractFromUVCI(
    jfn: JsonFunctions,
    operand: JsonNode,
    index: JsonNode,
    data: JsonNode
): JsonNode {
    val evalOperand = evaluateLogic(jfn, operand, data)
    if (!(evalOperand is NullNode || evalOperand is TextNode)) {
        throw RuntimeException(
            "\"UVCI\" argument (#1) of \"extractFromUVCI\" must be either a string or null"
        )
    }
    if (index !is IntNode) {
        throw RuntimeException(
            "\"index\" argument (#2) of \"extractFromUVCI\" must be an integer"
        )
    }
    val result = extractFromUVCI(
        if (evalOperand is TextNode) evalOperand.asText() else null,
        index.intValue()
    )
    return if (result == null) NullNode.instance else TextNode.valueOf(result)
}
