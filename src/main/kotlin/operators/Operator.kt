package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import de.rki.jfn.JsonFunctions

interface OperatorSet {

    val operators: Set<Operator>

    operator fun contains(operator: String): Boolean = find(operator) != null

    operator fun invoke(
        operator: String,
        jfn: JsonFunctions,
        args: JsonNode,
        data: JsonNode
    ): JsonNode {
        val op = find(operator) ?: error("Check `contains` first")
        return op(jfn, args, data)
    }

    operator fun plus(other: OperatorSet): OperatorSet {
        val operatorSet = operators + other.operators
        return object : OperatorSet {
            override val operators: Set<Operator> = operatorSet
        }
    }

    fun find(operator: String) = operators.find { it.operator == operator }
}

interface Operator {
    val operator: String
    operator fun invoke(
        jfn: JsonFunctions,
        args: JsonNode,
        data: JsonNode
    ): JsonNode
}
