/*
    Copied from:
    https://github.com/ehn-dcc-development/dgc-business-rules/blob/main/certlogic/certlogic-kotlin/src/main/kotlin/eu/ehn/dcc/certlogic/certlogic.kt

    Modifications Copyright (c) 2022 SAP SE or an SAP affiliate company.
*/

package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.error.argError
import de.rki.jfn.operators.AccessingDataOperator
import de.rki.jfn.operators.ArrayOperator
import de.rki.jfn.operators.ComparisonOperator
import de.rki.jfn.operators.ControlFlowOperator
import de.rki.jfn.operators.ExtractionOperator
import de.rki.jfn.operators.MathOperator
import de.rki.jfn.operators.StringOperator
import de.rki.jfn.operators.TimeOperator

private var operators = ArrayOperator +
    StringOperator +
    TimeOperator +
    MathOperator +
    AccessingDataOperator +
    ComparisonOperator +
    ControlFlowOperator +
    ExtractionOperator

fun evaluateLogic(
    jfn: JsonFunctions,
    logic: JsonNode,
    data: JsonNode
): JsonNode = when (logic) {
    is TextNode -> logic
    is NumericNode -> logic
    is BooleanNode -> logic
    is NullNode -> logic
    is ArrayNode -> {
        JsonNodeFactory.instance.arrayNode().addAll(logic.map { evaluateLogic(jfn, it, data) })
    }
    is ObjectNode -> {
        if (logic.size() != 1) {
            logic
        } else {
            val (operator, args) = logic.fields().next()
            when (operator) {
                in operators -> operators(operator, jfn, args, data)
                else -> argError("unrecognised operator: $operator")
            }
        }
    }
    else -> throw RuntimeException("invalid JsonFunctions expression: ${logic.toPrettyString()}")
}