package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode

interface OperatorSet {

    val operators: Set<Operator>

    operator fun contains(
        operator: String
    ): Boolean = findOperator(operator) != null

    operator fun invoke(
        operator: String,
        args: ArrayNode,
        data: JsonNode
    ): JsonNode {
        val op = findOperator(operator) ?: error("Check `contains` first")
        return op(args, data)
    }

    operator fun plus(other: OperatorSet): OperatorSet {
        val operatorSet = operators + other.operators
        return object : OperatorSet {
            override val operators: Set<Operator> = operatorSet
        }
    }

    private fun findOperator(operator: String) = operators.find { it.operator == operator }
}

interface Operator {
    operator fun invoke(args: ArrayNode, data: JsonNode): JsonNode
    val operator: String
}
