package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*

class JsonFunctionsEngine : JsonFunctions {

    private val registeredFunctions = mutableMapOf<String, JsonNode>()
    private val nodeFactory = JsonNodeFactory.instance

    override fun registerFunction(name: String, descriptor: JsonNode) {
        registeredFunctions[name] = descriptor
    }

    override fun evaluateFunction(name: String, parameters: JsonNode): Any? {
        val functionDescriptor = registeredFunctions[name] ?: throw NoSuchFunctionException()

        val functionDescriptorParameters = functionDescriptor.get("parameters") as ArrayNode
        val functionDescriptorLogic = functionDescriptor.get("logic") as ObjectNode

        return if (functionDescriptorParameters.size() == 0) {
            val emptyNode = ObjectMapper().readTree("{}")
            evaluate(functionDescriptorLogic, emptyNode)
        } else {
            TODO()
            // val data = determineData(functionDescriptor, parameters)
        }
    }

    fun determineData(functionDescriptor: JsonNode, parameters: JsonNode): JsonNode {
        nodeFactory.objectNode().apply {
            // functionDescriptor.filter {  }
        }

        TODO()

    }

    // TODO: add attribution for certlogic - this code was copied from it
    override fun isTruthy(value: JsonNode) = when (value) {
        is BooleanNode -> value == BooleanNode.TRUE
        is TextNode -> value.textValue().isNotEmpty()
        is IntNode -> value.intValue() != 0
        is ArrayNode -> value.size() > 0
        is ObjectNode -> value.size() > 0
        else -> false
    }

    override fun evaluate(logic: JsonNode, data: JsonNode) = when (logic) {
        is TextNode -> logic.textValue()
        is IntNode -> logic.intValue()
        is BooleanNode -> logic.booleanValue()
        is NullNode -> null
        is ObjectNode -> {
            if (logic.size() != 1) {
                throw RuntimeException("unrecognised expression object encountered")
            }

            val (operator, args) = logic.fields().next()
            if (!(args is ArrayNode && args.size() > 0)) {
                throw RuntimeException("operation not of the form { \"<operator>\": [ <args...> ] }")
            }
            when (operator) {
                "<" -> evaluateInfix(operator, args).booleanValue()
                else -> TODO()
            }
        }
        else -> TODO()
    }

    internal fun evaluateInfix(operator: String, args: ArrayNode): JsonNode {
        when (operator) {
            "and" -> if (args.size() < 2) throw RuntimeException("an \"and\" operation must have at least 2 operands")
            "<", ">", "<=", ">=", "after", "before", "not-after", "not-before" -> if (args.size() < 2 || args.size() > 3) throw RuntimeException(
                "an operation with operator \"$operator\" must have 2 or 3 operands"
            )
            else -> if (args.size() != 2) throw RuntimeException("an operation with operator \"$operator\" must have 2 operands")
        }
        // val evalArgs = args.map { arg -> evaluate(arg, data) }
        return when (operator) {
            "<", ">", "<=", ">=" -> {
                if (!args.all { it is IntNode }) {
                    throw RuntimeException("all operands of a comparison operator must be of integer type")
                }
                BooleanNode.valueOf(
                    compare(operator, args.map { (it as IntNode).intValue() })
                )
            }
            else -> TODO()
        }
    }

    internal fun <T : Comparable<T>> compare(operator: String, args: List<T>): Boolean =
        when (args.size) {
            2 -> intCompare(operator, args[0].compareTo(args[1]), 0)
            3 -> intCompare(operator, args[0].compareTo(args[1]), 0) && intCompare(
                operator,
                args[1].compareTo(args[2]),
                0
            )
            else -> throw RuntimeException("invalid number of operands to a \"$operator\" operation")
        }

    internal fun intCompare(operator: String, l: Int, r: Int): Boolean =
        when (operator) {
            "<" -> l < r
            ">" -> l > r
            "<=" -> l <= r
            ">=" -> l >= r
            else -> throw RuntimeException("unhandled comparison operator \"$operator\"")
        }
}

class NoSuchFunctionException : Throwable("No such function was registered in the engine")
