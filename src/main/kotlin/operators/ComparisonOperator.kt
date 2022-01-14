package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import de.rki.jfn.evaluateLogic

enum class ComparisonOperator: Operator {

    StrictEquality {
        override val operator = "==="

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val evalArgs = args.map { arg -> evaluateLogic(arg,data) }
            return BooleanNode.valueOf(evalArgs[0] == evalArgs[1])
        }
    },

    LooseEquality {
        override val operator = "=="

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val evalArgs = args.map { arg -> evaluateLogic(arg,data) }
            return BooleanNode.valueOf(evalArgs[0].asText() == evalArgs[1].asText())
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
    };

    companion object : OperatorSet {
        override val operators: Set<Operator> get() = values().toSet()
    }
}