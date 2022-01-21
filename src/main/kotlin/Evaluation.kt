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
import de.rki.jfn.operators.Operators

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

    is ObjectNode -> when {
        logic.size() != 1 -> logic
        else -> {
            val (operator, args) = logic.fields().next()
            when (operator) {
                in Operators -> Operators(operator, jfn, args, data)
                else -> throw UnsupportedOperationException("Unrecognised operator: $operator")
            }
        }
    }
    else -> argError("Invalid JsonFunctions expression: ${logic.toPrettyString()}")
}
