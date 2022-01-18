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
import de.rki.jfn.operators.MathOperator
import de.rki.jfn.operators.ControlFlowOperator
import de.rki.jfn.operators.StringOperator
import de.rki.jfn.operators.TimeOperator

fun evaluateLogic(logic: JsonNode, data: JsonNode): JsonNode = when (logic) {
    is TextNode -> logic
    is NumericNode -> logic
    is BooleanNode -> logic
    is NullNode -> logic
    is ArrayNode -> {
        JsonNodeFactory.instance.arrayNode().addAll(logic.map { evaluateLogic(it, data) })
    }
    is ObjectNode -> {
        if (logic.size() != 1) {
            throw RuntimeException(
                "unrecognised expression object encountered `${logic.toPrettyString()}`"
            )
        }
        val (operator, args) = logic.fields().next()
        if (operator == "var") {
            if (args.isArray && !args.isEmpty && args.first().isObject) {
                // var declares an operation
                evaluateLogic(args.first(), data)
            } else {
                evaluateVar(args, data)
            }
        } else {
            if (args !is ArrayNode) {
                throw RuntimeException(
                    "operation not of the form { \"<operator>\": [ <args...> ] } " +
                        "args=${args.toPrettyString()}"
                )
            }

            val operators = ArrayOperator +
                StringOperator +
                TimeOperator +
                MathOperator +
                AccessingDataOperator +
                ControlFlowOperator // Add new operators

            when (operator) {
                in operators -> operators(operator, args, data)
                "===", "and", ">", "<", ">=", "<=", "in", "+" -> evaluateInfix(operator, args, data)
                "!" -> evaluateNot(args[0], data)
                "!==" -> TODO()
                "extractFromUVCI" -> evaluateExtractFromUVCI(args[0], args[1], data)
                else -> throw RuntimeException("unrecognised operator: \"$operator\"")
            }
        }
    }
    else -> throw RuntimeException("invalid JsonFunctions expression: ${logic.toPrettyString()}")
}

internal fun evaluateVar(args: JsonNode, data: JsonNode): JsonNode {

    val path = when {
        args.isArray -> {
            if (args.isEmpty) {
                return data
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

internal fun evaluateInfix(
    operator: String,
    args: ArrayNode,
    data: JsonNode
): JsonNode {
    when (operator) {
        "and" -> if (args.size() < 2) throw IllegalArgumentException(
            "an \"$operator\"  operation must have at least 2 operands"
        )
        "<", ">", "<=", ">=" ->
            if (args.size() !in 2..3) throw IllegalArgumentException(
                "an operation with operator \"$operator\" must have 2 or 3 operands"
            )

        else -> if (args.size() != 2) throw IllegalArgumentException(
            "an operation with operator \"$operator\" must have 2 operands"
        )
    }
    val evalArgs = args.map { arg -> evaluateLogic(arg, data) }
    return when (operator) {
        "===" -> BooleanNode.valueOf(evalArgs[0] == evalArgs[1])
        "in" -> {
            val r = evalArgs[1]
            if (r !is ArrayNode) {
                throw RuntimeException("right-hand side of an \"in\" operation must be an array")
            }
            BooleanNode.valueOf(r.contains(evalArgs[0]))
        }
        "and" -> args.fold(BooleanNode.TRUE as JsonNode) { acc, current ->
            when {
                isValueFalsy(acc) -> acc
                isValueTruthy(acc) -> evaluateLogic(current, data)
                else -> throw RuntimeException(
                    "all operands of an \"and\" operation must be either truthy or falsy"
                )
            }
        }
        "<", ">", "<=", ">=" -> {
            if (!evalArgs.all { it is IntNode }) {
                throw RuntimeException(
                    "all operands of a comparison operator must be of integer type"
                )
            }
            BooleanNode.valueOf(
                compare(operator, evalArgs.map { (it as IntNode).intValue() })
            )
        }
        else -> throw RuntimeException("unhandled infix operator \"$operator\"")
    }
}

internal fun evaluateNot(
    operandExpr: JsonNode,
    data: JsonNode
): JsonNode {
    val operand = evaluateLogic(operandExpr, data)
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
    operand: JsonNode,
    index: JsonNode,
    data: JsonNode
): JsonNode {
    val evalOperand = evaluateLogic(operand, data)
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
