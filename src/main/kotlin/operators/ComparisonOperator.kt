package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.JsonFunctions
import de.rki.jfn.common.toBooleanNode
import de.rki.jfn.compare
import de.rki.jfn.error.argError
import de.rki.jfn.evaluateLogic
import de.rki.jfn.isTruthy
import de.rki.jfn.isValueFalsy

enum class ComparisonOperator : Operator {

    /*
    Strict equality means that the type and the value of two properties must be equal
     */
    StrictEquality {
        override val operator = "==="

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val evalArgs = jfn.evaluate(args, data)

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

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val evalArgs = jfn.evaluate(args, data)

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

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val evalArgs = jfn.evaluate(args, data)

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

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val evalArgs = jfn.evaluate(args, data)
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

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            throwOnIllegalSizeOfArgs(args, operator)
            val evalArgs = jfn.evaluate(args, data)
            return BooleanNode.valueOf(compare(">", evalArgs.map { mapToDouble(it) }))
        }
    },

    GreaterOrEqualsThan {
        override val operator = ">="

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            throwOnIllegalSizeOfArgs(args, operator)
            val evalArgs = jfn.evaluate(args, data)
            return BooleanNode.valueOf(compare(">=", evalArgs.map { mapToDouble(it) }))
        }
    },

    LessThan {
        override val operator = "<"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            throwOnIllegalSizeOfArgs(args, operator)
            val evalArgs = jfn.evaluate(args, data)
            return BooleanNode.valueOf(compare("<", evalArgs.map { mapToDouble(it) }))
        }
    },

    LessOrEqualsThan {
        override val operator = "<="

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            throwOnIllegalSizeOfArgs(args, operator)
            val evalArgs = jfn.evaluate(args, data)
            return BooleanNode.valueOf(compare("<=", evalArgs.map { mapToDouble(it) }))
        }
    },

    In {
        override val operator = "in"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val evalArgs = jfn.evaluate(args, data)

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

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            return args.fold(BooleanNode.TRUE as JsonNode) { acc, current ->
                when {
                    isValueFalsy(acc) -> acc
                    jfn.isTruthy(acc) -> jfn.evaluate(current, data)
                    else -> throw RuntimeException(
                        "all operands of an \"and\" operation must be either truthy or falsy"
                    )
                }
            }
        }
    },

    /**
     * @return First [isTruthy] argument or last argument
     */
    OR {
        override val operator = "or"

        override fun invoke(args: JsonNode, data: JsonNode): JsonNode = when (args.isEmpty) {
            true -> argError("Operator '$operator' requires at least one argument")
            false -> args.firstOrNull {
                evaluateLogic(logic = it, data = data).isTruthy
            } ?: evaluateLogic(logic = args.last(), data = data)
        }
    },

    /**
     * @return True if argument [isTruthy], false otherwise
     */
    DOUBLE_BANG {
        override val operator: String = "!!"

        override fun invoke(
            args: JsonNode,
            data: JsonNode
        ): JsonNode = evaluateLogic(logic = args, data = data)
            .firstOrNull() // to handle an empty node
            .let { it?.isTruthy ?: false }
            .toBooleanNode()
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

private fun throwOnIllegalSizeOfArgs(args: JsonNode, operator: String) {
    if (args.size() !in 2..3) argError(
        "an operation with operator \"$operator\" must have 2 or 3 operands"
    )
}
