package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import de.rki.jfn.common.toBooleanNode
import de.rki.jfn.error.argError
import de.rki.jfn.evaluateLogic
import de.rki.jfn.isTruthy

enum class ComparisonOperator : Operator {

    /**
     * @return First [isTruthy] argument or last argument
     */
    OR {
        override val operator = "or"

        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode = when (args.isEmpty) {
            true -> argError("Operator '$operator' requires at least one argument")
            false -> args.firstOrNull {
                evaluateLogic(logic = it, data = data).isTruthy
            } ?: evaluateLogic(logic = args.last(), data = data)
        }
    },

    /**
     * @return True if argument [isTruthy], false otherwise
     */
    DOUBLE_BANG {
        override val operator: String = "!!"

        override fun invoke(
            args: ArrayNode,
            data: JsonNode
        ): JsonNode = evaluateLogic(logic = args, data = data)
            .firstOrNull() // to handle an empty node
            .let { it?.isTruthy ?: false }
            .toBooleanNode()
    };

    companion object : OperatorSet {
        override val operators: Set<Operator> get() = values().toSet()
    }
}
