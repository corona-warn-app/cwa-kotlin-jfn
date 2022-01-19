package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.compare
import de.rki.jfn.error.argError
import de.rki.jfn.evaluateLogic
import de.rki.jfn.isValueFalsy
import de.rki.jfn.isValueTruthy

enum class ComparisonOperator : Operator {

    /*
    Strict equality means that the type and the value of two properties must be equal
     */
    StrictEquality {
        override val operator = "==="

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val evalArgs = evaluateLogic(args, data)

            val left = evalArgs[0]
            val right = evalArgs[1]

            return if (left.isNumber and right.isNumber) {
                val isEqual = left.doubleValue() == right.doubleValue()
                BooleanNode.valueOf(isEqual)
            } else {
                val isEqual = left == right
                BooleanNode.valueOf(isEqual)
            }
        }
    },

    /*
    Loose equality means that the string representations of two values must be equal.
    This means that e.g. that "1" (String) and 1 (Integer) are the same
     */
    LooseEquality {
        override val operator = "=="

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val evalArgs = evaluateLogic(args, data)

            val left = evalArgs[0]
            val right = evalArgs[1]

            return if (left.isNumber or right.isNumber) {
                try {
                    val rightDouble = mapToDouble(right)
                    val leftDouble = mapToDouble(left)

                    val isEqual = leftDouble == rightDouble
                    BooleanNode.valueOf(isEqual)
                } catch (exception: NumberFormatException) {
                    BooleanNode.FALSE
                }
            } else {
                val isEqual = left.asText() == right.asText()
                BooleanNode.valueOf(isEqual)
            }
        }
    },

    StrictInequality {
        override val operator = "!=="

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val evalArgs = evaluateLogic(args, data)

            val left = evalArgs[0]
            val right = evalArgs[1]

            return if (left.isNumber and right.isNumber) {
                val isEqual = left.doubleValue() != right.doubleValue()
                BooleanNode.valueOf(isEqual)
            } else {
                val isEqual = left != right
                BooleanNode.valueOf(isEqual)
            }
        }
    },

    LooseInequality {
        override val operator = "!="

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val evalArgs = evaluateLogic(args, data)
            val left = evalArgs[0]
            val right = evalArgs[1]

            return if (left.isNumber or right.isNumber) {
                try {
                    val rightDouble = mapToDouble(right)
                    val leftDouble = mapToDouble(left)

                    val isEqual = leftDouble != rightDouble
                    BooleanNode.valueOf(isEqual)
                } catch (exception: NumberFormatException) {
                    BooleanNode.TRUE
                }
            } else {
                val isEqual = left.asText() != right.asText()
                BooleanNode.valueOf(isEqual)
            }
        }
    },

    GreaterThan {
        override val operator = ">"

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            throwOnIllegalSizeOfArgs(args, operator)
            val evalArgs = evaluateLogic(args, data)
            return BooleanNode.valueOf(compare(">", evalArgs.map { mapToDouble(it) }))
        }
    },

    GreaterOrEqualsThan {
        override val operator = ">="

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            throwOnIllegalSizeOfArgs(args, operator)
            val evalArgs = evaluateLogic(args, data)
            return BooleanNode.valueOf(compare(">=", evalArgs.map { mapToDouble(it) }))
        }
    },

    LessThan {
        override val operator = "<"

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            throwOnIllegalSizeOfArgs(args, operator)
            val evalArgs = evaluateLogic(args, data)
            return BooleanNode.valueOf(compare("<", evalArgs.map { mapToDouble(it) }))
        }
    },

    LessOrEqualsThan {
        override val operator = "<="

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            throwOnIllegalSizeOfArgs(args, operator)
            val evalArgs = evaluateLogic(args, data)
            return BooleanNode.valueOf(compare("<=", evalArgs.map { mapToDouble(it) }))
        }
    },

    In {
        override val operator = "in"

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val evalArgs = evaluateLogic(args, data)

            val left = evalArgs[0]
            val right = evalArgs[1]

            if (left.isNull and right.isNull) {
                return BooleanNode.FALSE
            }

            val leftString = left.asText()
            return if (right.isArray) {
                val contains = right.any { it.asText() == leftString }
                BooleanNode.valueOf(contains)
            } else {
                val rightString = right.asText()
                val contains = rightString.contains(leftString)
                BooleanNode.valueOf(contains)
            }
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

// tries to map a JsonNode to a double value or throws an NumberFormatException if it can't
private fun mapToDouble(it: JsonNode): Double = when (it) {
    is NumericNode -> it.doubleValue()
    is TextNode -> it.textValue().toDouble()
    else -> {
        throw NumberFormatException()
    }
}

private fun throwOnIllegalSizeOfArgs(args: ArrayNode, operator: String) {
    if (args.size() !in 2..3) argError(
        "an operation with operator \"$operator\" must have 2 or 3 operands"
    )
}
