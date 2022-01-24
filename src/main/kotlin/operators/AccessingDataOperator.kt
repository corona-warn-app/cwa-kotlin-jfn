package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import de.rki.jfn.JsonFunctions
import de.rki.jfn.evaluateLogic

internal enum class AccessingDataOperator : Operator {
    Var {
        override val operator = "var"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val path = when {
                args.isArray && args.isEmpty -> return data
                args.isArray && args.first().isObject -> {
                    // argument is an operation, so let's evaluate it
                    evaluateLogic(jfn, args.first(), data).asText()
                }
                args.isArray && args.size() == 1 -> args.first().asText()
                args.isArray && args.size() > 1 -> {
                    // return last element of array if the argument is an array with more than 1 element
                    return args.last()
                }
                args.isNull || args.asText() == "" -> return data
                else -> args.asText()
            }

            return path.split(".").fold(data) { acc, fragment ->
                if (acc is NullNode) {
                    acc
                } else {
                    try {
                        val index = fragment.toInt()
                        if (acc is ArrayNode) acc[index] else null
                    } catch (e: NumberFormatException) {
                        if (acc is ObjectNode) acc[fragment] else null
                    } ?: NullNode.instance
                }
            }
        }
    },

    Missing {
        override val operator = "missing"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val missing = JsonNodeFactory.instance.arrayNode()
            val keys = args[0] as? ArrayNode // Array
                ?: args as? ArrayNode // Array
                ?: (args as? ObjectNode)?.let { jfn.evaluate(args, data) } // Object -> evaluate
                ?: JsonNodeFactory.instance.arrayNode().add(args) // Node -> Array

            for (key in keys) {
                val logic = JsonNodeFactory.instance.objectNode().set<ObjectNode>("var", key)
                val value = jfn.evaluate(logic, data)
                if (value is NullNode || value.asText() == "") missing.add(key)
            }
            return missing
        }
    },

    MissingSome {
        override val operator = "missing_some"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val arrayNode = JsonNodeFactory.instance.arrayNode()
            val min = args[0].asInt()
            val keys = args[1] as? ArrayNode ?: arrayNode
            val missing = Missing(jfn, keys, data)
            return when {
                keys.size() - missing.size() >= min -> arrayNode
                else -> missing
            }
        }
    };

    companion object : OperatorSet {
        override val operators: Set<Operator> get() = values().toSet()
    }
}
