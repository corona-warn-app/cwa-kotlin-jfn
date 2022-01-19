package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.common.toNumericNode
import de.rki.jfn.error.argError
import de.rki.jfn.evaluateLogic

enum class MathOperator : Operator {
    Plus {
        override val operator: String = "+"
        override fun invoke(args: JsonNode, data: JsonNode): JsonNode = evaluate(
            args = args,
            data = data,
            requiresTwoOperands = false,
            mathOperation = Double::plus
        )
    },

    Minus {
        override val operator: String = "-"
        override fun invoke(args: JsonNode, data: JsonNode): JsonNode = evaluate(
            args = args,
            data = data,
            requiresTwoOperands = true,
            mathOperation = Double::minus
        )
    },

    Multiplication {
        override val operator: String = "*"
        override fun invoke(args: JsonNode, data: JsonNode): JsonNode = evaluate(
            args = args,
            data = data,
            requiresTwoOperands = false,
            mathOperation = Double::times
        )
    },

    Division {
        override val operator: String = "/"
        override fun invoke(args: JsonNode, data: JsonNode): JsonNode = evaluate(
            args = args,
            data = data,
            requiresTwoOperands = true,
            mathOperation = Double::div
        )
    },

    Modulo {
        override val operator: String = "%"
        override fun invoke(args: JsonNode, data: JsonNode): JsonNode = evaluate(
            args = args,
            data = data,
            requiresTwoOperands = true,
            mathOperation = Double::rem
        )
    };

    companion object : OperatorSet {
        override val operators: Set<Operator>
            get() = values().toSet()
    }
}

private fun MathOperator.evaluate(
    args: JsonNode,
    data: JsonNode,
    requiresTwoOperands: Boolean,
    mathOperation: MathOperation
): NumericNode {
    val node = evaluateLogic(logic = args, data = data)
    return when {
        node.size() == 1 && this == MathOperator.Minus -> node.first().number * (-1)

        requiresTwoOperands -> when (node.size() == 2) {
            true -> mathOperation(node[0].number, node[1].number)
            false -> argError("Operator '$operator' requires two operands")
        }

        else -> node.map { it.number }
            .reduce { acc, i -> mathOperation(acc, i) }
    }.toNumericNode()
}

private typealias MathOperation = (Double, Double) -> Double

private val JsonNode.number: Double
    get() = when (this) {
        is NumericNode -> doubleValue()
        is TextNode -> textValue().toDouble()
        is BooleanNode -> booleanValue().compareTo(false).toDouble()
        else -> argError("Cannot convert value of $this to a double")
    }
