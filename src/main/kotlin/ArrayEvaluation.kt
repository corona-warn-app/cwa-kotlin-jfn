package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode

sealed interface Operator {
    operator fun contains(operator: String): Boolean
    operator fun invoke(operator: String, args: ArrayNode, data: JsonNode): JsonNode
}

enum class ArrayOperator {
    Reduce {
        override val operator = "reduce"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val operand: JsonNode = args[0]
            val lambda: JsonNode = args[1]
            val initial: JsonNode = args[2]

            val evalOperand = evaluateLogic(operand, data)
            val evalInitial = { evaluateLogic(initial, data) }

            if (evalOperand !is ArrayNode) return evalInitial()
            return evalOperand.foldIndexed(evalInitial()) { index, accumulator, current ->
                evaluateLogic(
                    lambda,
                    JsonNodeFactory.instance.objectNode()
                        .set<ObjectNode>("accumulator", accumulator)
                        .set<ObjectNode>("current", current)
                        .set<ObjectNode>("__index__", IntNode.valueOf(index))
                        .setAll(data as ObjectNode) // Add other `var` values in `data`
                )
            }
        }
    },

    Filter {
        override val operator = "filter"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            // TODO
            return JsonNodeFactory.instance.objectNode()
        }
    },

    Map {
        override val operator = "map"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            // TODO
            return JsonNodeFactory.instance.objectNode()
        }
    },

    Find {
        override val operator = "find"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            // TODO
            return JsonNodeFactory.instance.objectNode()
        }
    },

    All {
        override val operator = "all"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {

            val evalOperand = evaluateLogic(args[0], data)
            if (evalOperand !is ArrayNode) return BooleanNode.FALSE
            // TODO
            return JsonNodeFactory.instance.objectNode()
        }
    },

    None {
        override val operator = "none"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            // TODO
            return JsonNodeFactory.instance.objectNode()
        }
    },

    Some {
        override val operator = "some"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            // TODO
            return JsonNodeFactory.instance.objectNode()
        }
    },

    Count {
        override val operator = "count"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            // TODO
            return JsonNodeFactory.instance.objectNode()
        }
    },

    Push {
        override val operator = "push"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            // TODO
            return JsonNodeFactory.instance.objectNode()
        }
    },

    Sort {
        override val operator = "sort"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            // TODO
            return JsonNodeFactory.instance.objectNode()
        }
    },

    Merge {
        override val operator = "merge"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            // TODO
            return JsonNodeFactory.instance.objectNode()
        }
    },

    Max {
        override val operator = "max"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            // TODO
            return JsonNodeFactory.instance.objectNode()
        }
    },

    Min {
        override val operator = "min"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            // TODO
            return JsonNodeFactory.instance.objectNode()
        }
    },

    Cat {
        override val operator = "cat"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            // TODO
            return JsonNodeFactory.instance.objectNode()
        }
    };

    abstract operator fun invoke(args: ArrayNode, data: JsonNode): JsonNode
    abstract val operator: String

    companion object : Operator {
        override operator fun contains(
            operator: String
        ): Boolean = findOperator(operator) != null

        override operator fun invoke(
            operator: String,
            args: ArrayNode,
            data: JsonNode
        ): JsonNode {
            val op = findOperator(operator) ?: error("Check `contains` first")
            return op(args, data)
        }

        private fun findOperator(operator: String) = values().find { it.operator == operator }
    }
}
