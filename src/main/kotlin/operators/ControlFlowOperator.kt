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
            TODO("Not yet implemented")
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
            return data
        }

        override val operator = "declare"
    },

    Script {
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            try {
                args.forEach { evaluateLogic(it, data.deepCopy()) }
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
