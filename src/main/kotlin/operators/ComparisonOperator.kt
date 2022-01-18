package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
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