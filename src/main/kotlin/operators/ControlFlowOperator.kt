package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import de.rki.jfn.evaluateIf
import de.rki.jfn.evaluateInit
import de.rki.jfn.evaluateLogic

enum class ControlFlowOperator : Operator {
    Assign {
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val identifier = evaluateLogic(args[0], data)
            val value = evaluateLogic(args[1], data)

            if (!identifier.isTextual)
                throw IllegalArgumentException("First parameter of assign must be a string")

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

        override val operator = "assign"
    },

    Declare {
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val identifier = evaluateLogic(args[0], data)
            if (!identifier.isTextual)
                throw IllegalArgumentException("First parameter of declare must be a string")

            val value = evaluateLogic(args[1], data)
            data as ObjectNode
            data.replace(identifier.asText(), value)
            return NullNode.instance
        }

        override val operator = "declare"
    },

    Script {
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            try {
                args.forEach { evaluateLogic(it, data) }
            } catch (e: ReturnException) {
                return e.data
            }
            return NullNode.instance
        }

        override val operator = "script"
    },

    Init {
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            return evaluateInit(args, data)
        }

        override val operator = "init"
    },

    Return {
        override fun invoke(args: ArrayNode, data: JsonNode): Nothing {
            throw ReturnException(evaluateLogic(args.first(), data))
        }

        override val operator = "return"
    },

    If {
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            return evaluateIf(args, data)
        }

        override val operator: String = "if"
    },

    Ternary {
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            return evaluateIf(args, data)
        }

        override val operator = "?:"
    };

    companion object : OperatorSet {
        override val operators: Set<Operator> get() = values().toSet()
    }
}

private class ReturnException(
    val data: JsonNode
) : Exception()
