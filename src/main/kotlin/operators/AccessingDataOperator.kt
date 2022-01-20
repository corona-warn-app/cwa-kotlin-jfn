package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import de.rki.jfn.JsonFunctions

enum class AccessingDataOperator : Operator {

    Missing {
        override val operator = "missing"
        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val missing = JsonNodeFactory.instance.arrayNode()
            val keys = args[0] as? ArrayNode ?: args as? ArrayNode
                ?: JsonNodeFactory.instance.arrayNode().add(args)

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
