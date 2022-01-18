package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NumericNode
import de.rki.jfn.common.toIntNode
import de.rki.jfn.common.toNumericNode

enum class MathOperator : Operator {
    Plus {
        override val operator: String = "+"
        override fun invoke(
            args: ArrayNode,
            data: JsonNode
        ): JsonNode = args.doMathWithMultipleOperands(operator = operator) { l, r -> l + r }
    },

    Minus {
        override val operator: String = "-"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode = with(args) {
            when (size()) {
                1 -> (first().intValue() * (-1)).toIntNode()
                else -> doMathWithTwoOperands(operator = operator) { l, r -> l - r }
            }
        }
    },

    Multiplication {
        override val operator: String = "*"
        override fun invoke(
            args: ArrayNode,
            data: JsonNode
        ): JsonNode = args.doMathWithMultipleOperands(operator = operator) { l, r -> l * r }
    },

    Division {
        override val operator: String = "/"
        override fun invoke(
            args: ArrayNode,
            data: JsonNode
        ): JsonNode = args.doMathWithTwoOperands(operator = operator) { l, r -> l / r }
    },

    Modulo {
        override val operator: String = "%"
        override fun invoke(
            args: ArrayNode,
            data: JsonNode
        ): JsonNode = args.doMathWithTwoOperands(operator = operator) { l, r -> l % r }
    };

    companion object : OperatorSet {
        override val operators: Set<Operator>
            get() = values().toSet()
    }
}

private fun ArrayNode.doMathWithMultipleOperands(
    operator: String,
    mathOperation: (Double, Double) -> Double
): NumericNode = when (all { it is NumericNode }) {
    true -> map { it.doubleValue() }
        .reduce { acc, i -> mathOperation(acc, i) }
        .toNumericNode()
    false -> throw IllegalArgumentException("operands of a $operator operator must be integers")
}

private fun ArrayNode.doMathWithTwoOperands(
    operator: String,
    mathOperation: (Double, Double) -> Double
): NumericNode = when (size() == 2 && all { it is NumericNode }) {
    true -> mathOperation(get(0).doubleValue(), get(1).doubleValue()).toNumericNode()
    false -> throw IllegalArgumentException(
        "operands of a $operator operator must both be integers"
    )
}
