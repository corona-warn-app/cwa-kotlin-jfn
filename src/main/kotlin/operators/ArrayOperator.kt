package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.evaluateLogic
import de.rki.jfn.isValueTruthy

enum class ArrayOperator : Operator {
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
            val arrayNode = JsonNodeFactory.instance.arrayNode()
            val scopedData = evaluateLogic(args[0], data)
            val scopedLogic = args[1]
            val it = args[2]

            if (scopedData !is ArrayNode) return arrayNode

            if (it != null && !it.isTextual) {
                throw  IllegalArgumentException("Iteratee name must be a string")
            }

            val filterResult = when {
                it != null -> scopedData.filter { jsonNode ->
                    val mergedData = mergeData(it, jsonNode, data)
                    isValueTruthy(evaluateLogic(scopedLogic, mergedData))
                }

                else -> scopedData.filter { jsonNode ->
                    isValueTruthy(evaluateLogic(scopedLogic, jsonNode))
                }
            }

            return arrayNode.addAll(filterResult)
        }
    },

    Map {
        override val operator = "map"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val arrayNode = JsonNodeFactory.instance.arrayNode()
            val scopedData = evaluateLogic(args[0], data)
            val scopedLogic = args[1]
            val it = args[2]

            if (scopedData !is ArrayNode) return arrayNode

            val mapResult = when {
                it != null -> scopedData.map { jsonNode ->
                    val mergedData = mergeData(it, jsonNode, data)
                    evaluateLogic(scopedLogic, mergedData)
                }

                else -> scopedData.map { jsonNode ->
                    evaluateLogic(scopedLogic, jsonNode)
                }
            }

            return arrayNode.addAll(mapResult)
        }
    },

    Find {
        override val operator = "find"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val scopedData = evaluateLogic(args[0], data)
            val scopedLogic = args[1]
            val it = args[2]

            if (scopedData !is ArrayNode) return NullNode.instance
            return when {
                it != null -> scopedData.find { jsonNode ->
                    val mergedData = mergeData(it, jsonNode, data)
                    isValueTruthy(evaluateLogic(scopedLogic, mergedData))
                }

                else -> scopedData.find { jsonNode ->
                    isValueTruthy(evaluateLogic(scopedLogic, jsonNode))
                }

            } ?: NullNode.instance
        }
    },

    All {
        override val operator = "all"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val scopedData = evaluateLogic(args[0], data)
            val scopedLogic = args[1]
            val it = args[2]

            if (scopedData !is ArrayNode) return BooleanNode.FALSE

            scopedData.forEach { jsonNode ->
                val result = when {
                    it != null -> {
                        val mergedData = mergeData(it, jsonNode, data)
                        evaluateLogic(scopedLogic, mergedData)
                    }
                    else -> evaluateLogic(scopedLogic, jsonNode)
                }

                if (!isValueTruthy(result)) return BooleanNode.FALSE
            }
            return BooleanNode.TRUE
        }
    },

    None {
        override val operator = "none"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val objectNode = JsonNodeFactory.instance.objectNode().set<ObjectNode>("filter", args)
            val filtered = evaluateLogic(objectNode, data)
            return BooleanNode.valueOf(filtered.size() == 0)
        }
    },

    Some {
        override val operator = "some"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val objectNode = JsonNodeFactory.instance.objectNode().set<ObjectNode>("filter", args)
            val filtered = evaluateLogic(objectNode, data)
            return BooleanNode.valueOf(filtered.size() > 0)
        }
    },

    Count {
        override val operator = "count"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val scopedData = evaluateLogic(args[0], data)
            val size = if (scopedData is ArrayNode) scopedData.size() else 0
            return IntNode.valueOf(size)
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
            val arrayNode = JsonNodeFactory.instance.arrayNode()
            val scopedData = evaluateLogic(args[0], data)
            val scopedLogic = args[1]

            if (scopedData !is ArrayNode) return arrayNode

            val sortResult = scopedData.sortedWith { a, b ->

                val mergedData = JsonNodeFactory.instance.objectNode()
                    .setAll<ObjectNode>(data as ObjectNode)
                    .set<ObjectNode>("a", a)
                    .set<ObjectNode>("b", b)

                when (evaluateLogic(scopedLogic, mergedData)) {
                    BooleanNode.TRUE -> 1
                    BooleanNode.FALSE -> -1
                    else -> 0
                }
            }
            return arrayNode.addAll(sortResult)
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
            val scopedData = evaluateLogic(args, data)
            return IntNode.valueOf(scopedData.maxOf { it.intValue() })
        }
    },

    Min {
        override val operator = "min"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val scopedData = evaluateLogic(args, data)
            return IntNode.valueOf(scopedData.minOf { it.intValue() })
        }
    },

    Cat {
        override val operator = "cat"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val scopedData = evaluateLogic(args, data)
            val joinResult = scopedData.joinToString(separator = "") { it.asText() }
            return TextNode.valueOf(joinResult)
        }
    };


    fun mergeData(it: JsonNode, jsonNode: JsonNode, data: JsonNode): JsonNode {
        return JsonNodeFactory.instance.objectNode()
            .set<ObjectNode>(it.asText(), jsonNode)
            .setAll(data as ObjectNode)
    }

    companion object : OperatorSet {
        override val operators: Set<Operator> get() = values().toSet()
    }
}
