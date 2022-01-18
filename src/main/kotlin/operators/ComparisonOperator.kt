package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.compare
import de.rki.jfn.error.argError
import de.rki.jfn.evaluateLogic
import de.rki.jfn.isValueFalsy
import de.rki.jfn.isValueTruthy

enum class ComparisonOperator: Operator {

    /*
    Strict equality means that the type and the value of two properties must be equal
     */
    StrictEquality {
        override val operator = "==="

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val evalArgs =evaluateLogic(args,data)
            return BooleanNode.valueOf(evalArgs[0] == evalArgs[1])
        }
    },

    /*
    Loose equality means that the string representations of two values must be equal.
    This means that e.g. that "1" (String) and 1 (Integer) are the same
     */
    LooseEquality {
        override val operator = "=="

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val evalArgs = evaluateLogic(args,data)
            return BooleanNode.valueOf(evalArgs[0].asText() == evalArgs[1].asText())
        }
    },

    StrictInequality {
        override val operator = "!=="

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val evalArgs = evaluateLogic(args,data)
            return BooleanNode.valueOf(evalArgs[0] != evalArgs[1])
        }
    },

    LooseInequality {
        override val operator = "!="

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val evalArgs = evaluateLogic(args,data)
            return BooleanNode.valueOf(evalArgs[0].asText() != evalArgs[1].asText())
        }
    },

    GreaterThan {
        override val operator = ">"

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            throwOnIllegalSizeOfArgs(args, operator)
            val evalArgs = evaluateLogic(args, data)
            return BooleanNode.valueOf(compare(">", evalArgs.map { mapToInt(it) }))
        }
    },

    GreaterOrEqualsThan {
        override val operator = ">="

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            throwOnIllegalSizeOfArgs(args, operator)
            val evalArgs = evaluateLogic(args, data)
            return BooleanNode.valueOf(compare(">=", evalArgs.map { mapToInt(it) }))
        }
    },

    LessThan {
        override val operator = "<"

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            throwOnIllegalSizeOfArgs(args, operator)
            val evalArgs = evaluateLogic(args, data)
            return BooleanNode.valueOf(compare("<", evalArgs.map { mapToInt(it) }))
        }
    },

    LessOrEqualsThan {
        override val operator = "<="

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            throwOnIllegalSizeOfArgs(args, operator)
            val evalArgs = evaluateLogic(args, data)
            return BooleanNode.valueOf(compare("<=", evalArgs.map { mapToInt(it) }))
        }
    },

    In {
        override val operator = "in"

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val evalArgs = args.map { arg -> evaluateLogic(arg,data) }
            val r = evalArgs[1]
            if (r !is ArrayNode) {
                BooleanNode.FALSE
            }
            return BooleanNode.valueOf(r.contains(evalArgs[0]))
        }
    },

    And {
        override val operator = "and"

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            return args.fold(BooleanNode.TRUE as JsonNode) { acc, current ->
                when {
                    isValueFalsy(acc) -> acc
                    isValueTruthy(acc) -> evaluateLogic(current, data)
                    else -> throw RuntimeException(
                        "all operands of an \"and\" operation must be either truthy or falsy"
                    )
                }
            }
        }
    };

    companion object : OperatorSet {
        override val operators: Set<Operator> get() = values().toSet()
    }
}

// tries to map a JsonNode to an integer value or throws an IllegalArgumentException if it can't
private fun mapToInt(it: JsonNode): Int = when (it) {
    is IntNode -> it.intValue()
    is TextNode -> {
        try {
            it.textValue().toInt()
        } catch (exception: NumberFormatException) {
            argError(
                "operand of a comparison operator is not an integer and" +
                    "cant be converted to an int"
            )
        }
    }
    else -> {
        argError("operand of a comparison operator has invalid type")
    }
}

private fun throwOnIllegalSizeOfArgs(args: ArrayNode, operator: String) {
    if (args.size() !in 2..3) argError(
        "an operation with operator \"$operator\" must have 2 or 3 operands"
    )
}