package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode

interface OperatorSet {
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

    val operators: Set<Operator>

    private fun findOperator(operator: String) = operators.find { it.operator == operator }
}

interface Operator {
    val operator: String
    operator fun invoke(args: ArrayNode, data: JsonNode): JsonNode
}
