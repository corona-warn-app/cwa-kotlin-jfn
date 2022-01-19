package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import de.rki.jfn.evaluateAfter
import de.rki.jfn.evaluateBefore
import de.rki.jfn.evaluateDiffTime
import de.rki.jfn.evaluateLogic
import de.rki.jfn.evaluateNotAfter
import de.rki.jfn.evaluateNotBefore
import de.rki.jfn.evaluatePlusTime

enum class TimeOperator : Operator {
    DiffTime {
        override fun invoke(args: JsonNode, data: JsonNode): JsonNode {
            val arguments = args.map { arg -> evaluateLogic(arg, data) }
            return evaluateDiffTime(arguments)
        }

        override val operator = "diffTime"
    },

    PlusTime {
        override fun invoke(args: JsonNode, data: JsonNode): JsonNode {
            val arguments = args.map { arg -> evaluateLogic(arg, data) }
            return evaluatePlusTime(arguments)
        }

        override val operator = "plusTime"
    },

    After {
        override fun invoke(args: JsonNode, data: JsonNode): JsonNode {
            val arguments = args.map { arg -> evaluateLogic(arg, data) }
            return evaluateAfter(arguments)
        }

        override val operator = "after"
    },

    Before {
        override fun invoke(args: JsonNode, data: JsonNode): JsonNode {
            val arguments = args.map { arg -> evaluateLogic(arg, data) }
            return evaluateBefore(arguments)
        }

        override val operator = "before"
    },

    NotBefore {
        override fun invoke(args: JsonNode, data: JsonNode): JsonNode {
            val arguments = args.map { arg -> evaluateLogic(arg, data) }
            return evaluateNotBefore(arguments)
        }

        override val operator = "not-before"
    },

    NotAfter {
        override fun invoke(args: JsonNode, data: JsonNode): JsonNode {
            val arguments = args.map { arg -> evaluateLogic(arg, data) }
            return evaluateNotAfter(arguments)
        }

        override val operator = "not-after"
    };

    companion object : OperatorSet {
        override val operators: Set<Operator> get() = values().toSet()
    }
}
