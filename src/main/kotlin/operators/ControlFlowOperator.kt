package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import de.rki.jfn.JsonFunctions
import de.rki.jfn.ReturnException
import de.rki.jfn.evaluateAssign
import de.rki.jfn.evaluateDeclare
import de.rki.jfn.evaluateIf
import de.rki.jfn.evaluateInit
import de.rki.jfn.evaluateLogic
import de.rki.jfn.evaluateScript
import de.rki.jfn.evaluateTernary

enum class ControlFlowOperator : Operator {
    Assign {
        override val operator = "assign"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            return evaluateAssign(jfn, args, data)
        }
    },

    Declare {
        override val operator = "declare"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            return evaluateDeclare(jfn, args, data)
        }
    },

    Script {
        override val operator = "script"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            return evaluateScript(jfn, args, data)
        }
    },

    Init {
        override val operator = "init"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            return evaluateInit(jfn, args, data)
        }
    },

    Return {
        override val operator = "return"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): Nothing {
            throw ReturnException(evaluateLogic(jfn, args.first(), data))
        }
    },

    If {
        override val operator: String = "if"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            return evaluateIf(jfn, args, data)
        }
    },

    Ternary {
        override val operator = "?:"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            return evaluateTernary(jfn, args, data)
        }
    };

    companion object : OperatorSet {
        override val operators: Set<Operator> get() = values().toSet()
    }
}
