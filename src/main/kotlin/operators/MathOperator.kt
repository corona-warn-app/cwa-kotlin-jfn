package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import de.rki.jfn.common.toIntNode

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
    mathOperation: (Int, Int) -> Int
): IntNode = when (all { it is IntNode }) {
    true -> map { it.intValue() }
        .reduce { acc, i -> mathOperation(acc, i) }
        .toIntNode()
    false -> throw IllegalArgumentException("operands of a $operator operator must be integers")
}

private fun ArrayNode.doMathWithTwoOperands(
    operator: String,
    mathOperation: (Int, Int) -> Int
): IntNode = when (size() == 2 && all { it is IntNode }) {
    true -> mathOperation(get(0).intValue(), get(1).intValue()).toIntNode()
    false -> throw IllegalArgumentException(
        "operands of a $operator operator must both be integers"
    )
}
