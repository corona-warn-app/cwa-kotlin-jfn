package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import de.rki.jfn.JsonFunctions
import de.rki.jfn.evaluateAfter
import de.rki.jfn.evaluateBefore
import de.rki.jfn.evaluateDiffTime
import de.rki.jfn.evaluateNotAfter
import de.rki.jfn.evaluateNotBefore
import de.rki.jfn.evaluatePlusTime

internal enum class TimeOperator : Operator {
    DiffTime {
        override val operator = "diffTime"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val arguments = args.map { arg -> jfn.evaluate(arg, data) }
            return evaluateDiffTime(arguments)
        }
    },

    PlusTime {
        override val operator = "plusTime"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val arguments = args.map { arg -> jfn.evaluate(arg, data) }
            return evaluatePlusTime(arguments)
        }
    },

    After {
        override val operator = "after"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val arguments = args.map { arg -> jfn.evaluate(arg, data) }
            return evaluateAfter(arguments)
        }
    },

    Before {
        override val operator = "before"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val arguments = args.map { arg -> jfn.evaluate(arg, data) }
            return evaluateBefore(arguments)
        }
    },

    NotBefore {
        override val operator = "not-before"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val arguments = args.map { arg -> jfn.evaluate(arg, data) }
            return evaluateNotBefore(arguments)
        }
    },

    NotAfter {
        override val operator = "not-after"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val arguments = args.map { arg -> jfn.evaluate(arg, data) }
            return evaluateNotAfter(arguments)
        }
    };

    companion object : OperatorSet {
        override val operators: Set<Operator> get() = values().toSet()
    }
}
