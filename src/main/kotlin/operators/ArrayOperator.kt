package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.JsonFunctions
import de.rki.jfn.error.argError

enum class ArrayOperator : Operator {
    Reduce {
        override val operator = "reduce"
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val scopedData = jfn.evaluate(args[0], data)
            val scopedLogic = args[1]
            val initial = jfn.evaluate(args[2], data)
            if (scopedData !is ArrayNode) return initial
            return scopedData.foldIndexed(initial) { index, accumulator, current ->
                jfn.evaluate(
                    scopedLogic,
                    JsonNodeFactory.instance.objectNode()
                        .set<ObjectNode>("accumulator", accumulator)
                        .set<ObjectNode>("current", current)
                        .set<ObjectNode>("__index__", IntNode.valueOf(index))
                        .set<ObjectNode>("data", data)
                        .setAll(data as ObjectNode) // Add other `var` values in `data`
                )
            }
        }
    },

    Filter {
        override val operator = "filter"
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val arrayNode = JsonNodeFactory.instance.arrayNode()
            val scopedData = jfn.evaluate(args[0], data)
            val scopedLogic = args[1]
            val it = args[2]

            if (scopedData !is ArrayNode) return arrayNode
            if (it != null && !it.isTextual) argError("Iterator name must be a string")

            val filterResult = when {
                it != null -> scopedData.filter { jsonNode ->
                    val mergedData = mergeData(it, jsonNode, data)
                    jfn.isTruthy(jfn.evaluate(scopedLogic, mergedData))
                }

                else -> scopedData.filter { jsonNode ->
                    jfn.isTruthy(jfn.evaluate(scopedLogic, jsonNode))
                }
            }

            return arrayNode.addAll(filterResult)
        }
    },

    Map {
        override val operator = "map"
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val arrayNode = JsonNodeFactory.instance.arrayNode()
            val scopedData = jfn.evaluate(args[0], data)
            val scopedLogic = args[1]
            val it = args[2]

            if (scopedData !is ArrayNode) return arrayNode

            val mapResult = when {
                it != null -> scopedData.map { jsonNode ->
                    val mergedData = mergeData(it, jsonNode, data)
                    jfn.evaluate(scopedLogic, mergedData)
                }

                else -> scopedData.map { jsonNode ->
                    jfn.evaluate(scopedLogic, jsonNode)
                }
            }

            return arrayNode.addAll(mapResult)
        }
    },

    Find {
        override val operator = "find"
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val scopedData = jfn.evaluate(args[0], data)
            val scopedLogic = args[1]
            val it = args[2]

            if (scopedData !is ArrayNode) return NullNode.instance
            return when {
                it != null -> scopedData.find { jsonNode ->
                    val mergedData = mergeData(it, jsonNode, data)
                    jfn.isTruthy(jfn.evaluate(scopedLogic, mergedData))
                }

                else -> scopedData.find { jsonNode ->
                    jfn.isTruthy(jfn.evaluate(scopedLogic, jsonNode))
                }
            } ?: NullNode.instance
        }
    },

    All {
        override val operator = "all"
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val scopedData = jfn.evaluate(args[0], data)
            val scopedLogic = args[1]
            val it = args[2]

            // All of an empty set is false.
            if (scopedData !is ArrayNode || scopedData.isEmpty) return BooleanNode.FALSE

            scopedData.forEach { jsonNode ->
                val result = when {
                    it != null -> {
                        val mergedData = mergeData(it, jsonNode, data)
                        jfn.evaluate(scopedLogic, mergedData)
                    }
                    else -> jfn.evaluate(scopedLogic, jsonNode)
                }

                if (!jfn.isTruthy(result)) return BooleanNode.FALSE // First falsy, short circuit
            }
            return BooleanNode.TRUE // All were truthy
        }
    },

    None {
        override val operator = "none"
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val objectNode = JsonNodeFactory.instance.objectNode().set<ObjectNode>("filter", args)
            val filtered = jfn.evaluate(objectNode, data)
            return BooleanNode.valueOf(filtered.size() == 0)
        }
    },

    Some {
        override val operator = "some"
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val objectNode = JsonNodeFactory.instance.objectNode().set<ObjectNode>("filter", args)
            val filtered = jfn.evaluate(objectNode, data)
            return BooleanNode.valueOf(filtered.size() > 0)
        }
    },

    Count {
        override val operator = "count"
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val scopedData = jfn.evaluate(args[0], data)
            val size = if (scopedData is ArrayNode) scopedData.size() else 0
            return IntNode.valueOf(size)
        }
    },

    Push {
        override val operator = "push"
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val array = jfn.evaluate(args[0], data)
            if (array !is ArrayNode) argError("\"push\" first argument must be an array")
            for (i in 1 until args.size()) {
                array.add(jfn.evaluate(args[i], data))
            }
            return array
        }
    },

    Sort {
        override val operator = "sort"
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val arrayNode = JsonNodeFactory.instance.arrayNode()
            val scopedData = jfn.evaluate(args[0], data)
            val scopedLogic = args[1]

            if (scopedData !is ArrayNode) return arrayNode

            val sortResult = scopedData.sortedWith { a, b ->

                val mergedData = JsonNodeFactory.instance.objectNode()
                    .setAll<ObjectNode>(data as ObjectNode)
                    .set<ObjectNode>("a", a)
                    .set<ObjectNode>("b", b)

                when (jfn.evaluate(scopedLogic, mergedData)) {
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
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val scopedData = jfn.evaluate(args, data)

            val arrayNode = JsonNodeFactory.instance.arrayNode()
            if (scopedData !is ArrayNode) return arrayNode.add(scopedData)
            scopedData.forEach { jsonNode ->
                when (jsonNode) {
                    is ArrayNode -> arrayNode.addAll(jsonNode)
                    else -> arrayNode.add(jsonNode)
                }
            }
            return arrayNode
        }
    },

    Max {
        override val operator = "max"
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val scopedData = jfn.evaluate(args, data)
            return scopedData.maxByOrNull { it.asInt() } ?: NullNode.instance
        }
    },

    Min {
        override val operator = "min"
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val scopedData = jfn.evaluate(args, data)
            return scopedData.minByOrNull { it.asInt() } ?: NullNode.instance
        }
    },

    Cat {
        override val operator = "cat"
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val result = when (val scopedData = jfn.evaluate(args, data)) {
                is ArrayNode -> scopedData.joinToString(separator = "") { it.asText() }
                else -> scopedData.asText()
            }
            return TextNode.valueOf(result)
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
