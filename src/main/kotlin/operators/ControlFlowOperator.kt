package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import de.rki.jfn.error.argError
import de.rki.jfn.evaluateIf
import de.rki.jfn.evaluateInit
import de.rki.jfn.evaluateLogic

enum class ControlFlowOperator : Operator {
    Assign {
        override val operator = "assign"

        override fun invoke(args: JsonNode, data: JsonNode): JsonNode {
            val identifier = evaluateLogic(args[0], data)
            val value = evaluateLogic(args[1], data)

            if (!identifier.isTextual)
                argError("First parameter of assign must be a string")

            val identifierChunks = identifier.asText().split('.').toMutableList()
            val propertyName = identifierChunks.last()
            identifierChunks.removeLast()
            val newData = identifierChunks.fold(data) { acc, chunk ->
                if (acc.isArray) acc[Integer.valueOf(chunk)]
                else acc.get(chunk)
            }
            if (newData.isArray) {
                newData as ArrayNode
                val index = Integer.valueOf(propertyName).toInt()
                if (index < newData.size())
                    newData.set(index, value)
                else
                    newData.add(value)
            }
            else (newData as ObjectNode).replace(propertyName, value)

            return NullNode.instance
        }
    },

    Declare {
        override val operator = "declare"

        override fun invoke(args: JsonNode, data: JsonNode): JsonNode {
            val identifier = evaluateLogic(args[0], data)
            if (!identifier.isTextual)
                argError("First parameter of declare must be a string")

            val value = evaluateLogic(args[1], data)
            data as ObjectNode
            data.replace(identifier.asText(), value)
            return NullNode.instance
        }
    },

    Script {
        override val operator = "script"

        override fun invoke(args: JsonNode, data: JsonNode): JsonNode {
            val scopedData = ObjectNode(JsonNodeFactory.instance)
                .setAll<JsonNode>(data as ObjectNode)
            try {
                args.forEach { evaluateLogic(it, scopedData) }
            } catch (e: ReturnException) {
                return e.data
            }
            return NullNode.instance
        }
    },

    Init {
        override val operator = "init"

        override fun invoke(args: JsonNode, data: JsonNode): JsonNode {
            return evaluateInit(args, data)
        }
    },

    Return {
        override val operator = "return"

        override fun invoke(args: JsonNode, data: JsonNode): Nothing {
            throw ReturnException(evaluateLogic(args.first(), data))
        }
    },

    If {
        override val operator: String = "if"

        override fun invoke(args: JsonNode, data: JsonNode): JsonNode {
            return evaluateIf(args, data)
        }
    },

    Ternary {
        override val operator = "?:"

        override fun invoke(args: JsonNode, data: JsonNode): JsonNode {
            return evaluateIf(args, data)
        }
    };

    companion object : OperatorSet {
        override val operators: Set<Operator> get() = values().toSet()
    }
}

private class ReturnException(
    val data: JsonNode
) : Exception()
