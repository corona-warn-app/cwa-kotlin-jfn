package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*

class JsonFunctionsEngine : JsonFunctions {

    private val registeredFunctions = mutableMapOf<String, JsonNode>()
    private val nodeFactory = JsonNodeFactory.instance

    private val parametersPropertyName = "parameters"
    private val logicPropertyName = "logic"

    override fun registerFunction(name: String, descriptor: JsonNode) {

        if (!descriptor.has(parametersPropertyName)) {
            throw RuntimeException("descriptor must have a '$parametersPropertyName' property!")
        }
        if (descriptor.get(parametersPropertyName) !is ArrayNode) {
            throw RuntimeException("'$parametersPropertyName' of descriptor must be an array!")
        }

        if (!descriptor.has(logicPropertyName)) {
            throw RuntimeException("descriptor must have a '$logicPropertyName' property!")
        }
        if (descriptor.get(logicPropertyName) !is ObjectNode) {
            throw RuntimeException("'$logicPropertyName' of descriptor must be an object!")
        }

        registeredFunctions[name] = descriptor
    }

    override fun evaluateFunction(name: String, parameters: JsonNode): JsonNode {
        val functionDescriptor = registeredFunctions[name] ?: throw NoSuchFunctionException()

        val functionDescriptorParameters = functionDescriptor.get(parametersPropertyName) as ArrayNode
        val functionDescriptorLogic = functionDescriptor.get(logicPropertyName) as ObjectNode

        return evaluate(functionDescriptorLogic, determineData(functionDescriptorParameters, parameters))
    }

    fun determineData(parameters: ArrayNode, input: JsonNode): JsonNode {
        return nodeFactory.objectNode().apply {
            parameters.forEach {
                val propertyName = it.get("name").textValue()
                val propertyValue = if (input.has(propertyName)) {
                    input.get(propertyName)
                } else if (it.has("default")) {
                    it.get("default")
                } else {
                    throw RuntimeException("No value provided for $propertyName and also no default value defined.")
                }
                set<JsonNode>(propertyName, propertyValue)
            }
        }
    }

    override fun isTruthy(value: JsonNode) = when (value) {
        is BooleanNode -> value == BooleanNode.TRUE
        is TextNode -> value.textValue().isNotEmpty()
        is IntNode -> value.intValue() != 0
        is ArrayNode -> value.size() > 0
        is ObjectNode -> value.size() > 0
        else -> false
    }

    internal fun isFalsy(value: JsonNode): Boolean = when (value) {
        is BooleanNode -> value == BooleanNode.FALSE
        is NullNode -> true
        is TextNode -> value.textValue().isEmpty()
        is IntNode -> value.intValue() == 0
        is ArrayNode -> value.size() == 0
        is ObjectNode -> value.size() == 0
        else -> false
    }

    override fun evaluate(logic: JsonNode, data: JsonNode) = when (logic) {
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
                    throw RuntimeException("operation not of the form { \"<operator>\": [ <args...> ] }")
                }
                when (operator) {
                    "if" -> evaluateIf(args[0], args[1], args[2], data)
                    "===", "and", ">", "<", ">=", "<=", "in", "+", "after", "before", "not-after", "not-before" -> evaluateInfix(
                        operator,
                        args,
                        data
                    )
                    "!" -> evaluateNot(args[0], data)
                    // "plusTime" -> evaluatePlusTime(args[0], args[1], args[2], data)
                    "reduce" -> evaluateReduce(args[0], args[1], args[2], data)
                    // "extractFromUVCI" -> evaluateExtractFromUVCI(args[0], args[1], data)
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
        if (path == "") {  // "it"
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

    internal fun evaluateInfix(operator: String, args: ArrayNode, data: JsonNode): JsonNode {
        when (operator) {
            "and" -> if (args.size() < 2) throw RuntimeException("an \"and\" operation must have at least 2 operands")
            "<", ">", "<=", ">=", "after", "before", "not-after", "not-before" -> if (args.size() < 2 || args.size() > 3) throw RuntimeException(
                "an operation with operator \"$operator\" must have 2 or 3 operands"
            )
            else -> if (args.size() != 2) throw RuntimeException("an operation with operator \"$operator\" must have 2 operands")
        }
        val evalArgs = args.map { arg -> evaluate(arg, data) }
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
                    isFalsy(acc) -> acc
                    isTruthy(acc) -> evaluate(current, data)
                    else -> throw RuntimeException("all operands of an \"and\" operation must be either truthy or falsy")
                }
            }
            "<", ">", "<=", ">=" -> {
                if (!evalArgs.all { it is IntNode }) {
                    throw RuntimeException("all operands of a comparison operator must be of integer type")
                }
                BooleanNode.valueOf(
                    compare(operator, evalArgs.map { (it as IntNode).intValue() })
                )
            }
            /*"after", "before", "not-after", "not-before" -> {
                if (!evalArgs.all { it is JsonDateTime }) {
                    throw RuntimeException("all operands of a date-time comparsion must be date-times")
                }
                BooleanNode.valueOf(
                    compare(comparisonOperatorForDateTimeComparison(operator), evalArgs.map { (it as JsonDateTime).temporalValue() })
                )
            }*/
            else -> throw RuntimeException("unhandled infix operator \"$operator\"")
        }
    }

    internal fun evaluateIf(guard: JsonNode, then: JsonNode, else_: JsonNode, data: JsonNode): JsonNode {
        val evalGuard = evaluate(guard, data)
        if (isTruthy(evalGuard)) {
            return evaluate(then, data)
        }
        if (isFalsy(evalGuard)) {
            return evaluate(else_, data)
        }
        throw RuntimeException("if-guard evaluates to something neither truthy, nor falsy: $evalGuard")
    }

    internal fun evaluateNot(operandExpr: JsonNode, data: JsonNode): JsonNode {
        val operand = evaluate(operandExpr, data)
        if (isFalsy(operand)) {
            return BooleanNode.TRUE
        }
        if (isTruthy(operand)) {
            return BooleanNode.FALSE
        }
        throw RuntimeException("operand of ! evaluates to something neither truthy, nor falsy: $operand")
    }

    internal fun evaluateReduce(operand: JsonNode, lambda: JsonNode, initial: JsonNode, data: JsonNode): JsonNode {
        val evalOperand = evaluate(operand, data)
        val evalInitial = { evaluate(initial, data) }
        if (evalOperand == NullNode.instance) {
            return evalInitial()
        }
        if (evalOperand !is ArrayNode) {
            throw RuntimeException("operand of reduce evaluated to a non-null non-array")
        }
        return evalOperand.fold(evalInitial()) { accumulator, current ->
            evaluate(
                lambda,
                JsonNodeFactory.instance.objectNode()
                    .set<ObjectNode>("accumulator", accumulator)
                    .set<ObjectNode>("current", current)
            )
        }
    }
}

class NoSuchFunctionException : Throwable("No such function was registered in the engine")
