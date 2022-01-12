package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

fun evaluateLogic(logic: JsonNode, data: JsonNode) = when (logic) {
    is TextNode -> logic
    is IntNode -> logic
    is BooleanNode -> logic
    is NullNode -> logic
    is ObjectNode -> {
        if (logic.size() != 1) {
            throw RuntimeException("unrecognised expression object encountered")
        }
        val (operator, args) = logic.fields().next()
        if (operator == "var") {
            evaluateVar(args, data)
        } else {
            if (!(args is ArrayNode && args.size() > 0)) {
                throw RuntimeException(
                    "operation not of the form { \"<operator>\": [ <args...> ] }"
                )
            }
            when (operator) {
                "if" -> evaluateIf(args[0], args[1], args[2], data)
                "===", "and", ">", "<", ">=", "<=", "in", "+", "after", "before", "not-after",
                "not-before", "diffTime", "plusTime" -> evaluateInfix(operator, args, data)
                "!" -> evaluateNot(args[0], data)
                "!==" -> TODO()
                // "plusTime" -> evaluatePlusTime(args[0], args[1], args[2], data)
                "reduce" -> evaluateReduce(args[0], args[1], args[2], data)
                "extractFromUVCI" -> evaluateExtractFromUVCI(args[0], args[1], data)
                else -> throw RuntimeException("unrecognised operator: \"$operator\"")
            }
        }
    }
    else -> throw RuntimeException("invalid JsonFunctions expression: $logic")
}

internal fun evaluateVar(args: JsonNode, data: JsonNode): JsonNode {
    if (args !is TextNode) {
        throw RuntimeException("not of the form { \"var\": \"<path>\" }")
    }
    val path = args.asText()
    if (path == "") { // "it"
        return data
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
        "and" -> if (args.size() < 2) throw RuntimeException(
            "an \"and\" operation must have at least 2 operands"
        )
        "<", ">", "<=", ">=", "after", "before", "not-after", "not-before" ->
            if (args.size() < 2 || args.size() > 3) throw RuntimeException(
                "an operation with operator \"$operator\" must have 2 or 3 operands"
            )
        else -> if (args.size() != 2) throw RuntimeException(
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
        "+" -> {
            val l = evalArgs[0]
            val r = evalArgs[1]
            if (l !is IntNode || r !is IntNode) {
                throw RuntimeException("operands of a " + " operator must both be integers")
            }
            IntNode.valueOf(evalArgs[0].intValue() + evalArgs[1].intValue())
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
        "diffTime" -> evaluateDiffTime(evalArgs)
        "plusTime" -> evaluatePlusTime(evalArgs)
        "after" -> evaluateAfter(evalArgs)
        "before" -> evaluateBefore(evalArgs)
        "not-after" -> evaluateNotAfter(evalArgs)
        "not-before" -> evaluateNotBefore(evalArgs)
        else -> throw RuntimeException("unhandled infix operator \"$operator\"")
    }
}

internal fun evaluateIf(
    guard: JsonNode,
    then: JsonNode,
    else_: JsonNode,
    data: JsonNode
): JsonNode {
    val evalGuard = evaluateLogic(guard, data)
    if (isValueTruthy(evalGuard)) {
        return evaluateLogic(then, data)
    }
    if (isValueFalsy(evalGuard)) {
        return evaluateLogic(else_, data)
    }
    throw RuntimeException(
        "if-guard evaluates to something neither truthy, nor falsy: $evalGuard"
    )
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

internal fun evaluateReduce(
    operand: JsonNode,
    lambda: JsonNode,
    initial: JsonNode,
    data: JsonNode
): JsonNode {
    val evalOperand = evaluateLogic(operand, data)
    val evalInitial = { evaluateLogic(initial, data) }
    if (evalOperand == NullNode.instance) {
        return evalInitial()
    }
    if (evalOperand !is ArrayNode) {
        throw RuntimeException("operand of reduce evaluated to a non-null non-array")
    }
    return evalOperand.fold(evalInitial()) { accumulator, current ->
        evaluateLogic(
            lambda,
            JsonNodeFactory.instance.objectNode()
                .set<ObjectNode>("accumulator", accumulator)
                .set<ObjectNode>("current", current)
        )
    }
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
