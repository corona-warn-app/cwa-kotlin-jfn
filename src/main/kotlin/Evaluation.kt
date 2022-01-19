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
import de.rki.jfn.operators.AccessingDataOperator
import de.rki.jfn.operators.ArrayOperator
import de.rki.jfn.operators.ComparisonOperator
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
            throw RuntimeException(
                "unrecognised expression object encountered `${logic.toPrettyString()}`"
            )
        }

        val operators = ArrayOperator +
            StringOperator +
            TimeOperator +
            MathOperator +
            AccessingDataOperator +
            ComparisonOperator

        val (operator, args) = logic.fields().next()
        when (operator) {
            "var" -> evaluateVar(jfn, args, data)
            "!" -> evaluateNot(jfn, args, data)
            else -> {
                if (!(args is ArrayNode && args.size() > 0)) {
                    throw RuntimeException(
                        "operation not of the form { \"<operator>\": [ <args...> ] }"
                    )
                }
                when (operator) {
                    "if" -> evaluateIf(jfn, args[0], args[1], args[2], data)
                    in operators -> operators(operator, jfn, args, data)
                    "extractFromUVCI" -> evaluateExtractFromUVCI(jfn, args[0], args[1], data)
                    else -> throw RuntimeException("unrecognised operator: \"$operator\"")
                }
            }
        }
    }
    else -> throw RuntimeException("invalid JsonFunctions expression: ${logic.toPrettyString()}")
}

internal fun evaluateVar(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {

    val path = when {
        args.isArray -> {
            if (args.isEmpty) {
                return data
            }
            if (args.first().isObject) {
                // var declares an operation
                return evaluateLogic(jfn, args.first(), data)
            }
            if (args.size() == 1) {
                args.first().asText()
            } else {
                // return last element of array if var declares an array with more than 1 element
                return args.last()
            }
        }
        args.isNull || args.asText() == "" -> {
            return data
        }
        else -> args.asText()
    }

    return path.split(".").fold(data) { acc, fragment ->
        if (acc is NullNode) {
            acc
        } else {
            try {
                val index = Integer.parseInt(fragment, 10)
                if (acc is ArrayNode) acc[index] else null
            } catch (e: NumberFormatException) {
                if (acc is ObjectNode) acc[fragment] else null
            } ?: NullNode.instance
        }
    }
}

internal fun evaluateIf(
    jfn: JsonFunctions,
    guard: JsonNode,
    then: JsonNode,
    else_: JsonNode,
    data: JsonNode
): JsonNode {
    val evalGuard = evaluateLogic(jfn, guard, data)
    if (isValueTruthy(evalGuard)) {
        return evaluateLogic(jfn, then, data)
    }
    if (isValueFalsy(evalGuard)) {
        return evaluateLogic(jfn, else_, data)
    }
    throw RuntimeException(
        "if-guard evaluates to something neither truthy, nor falsy: $evalGuard"
    )
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
