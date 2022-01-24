/*
    Copied from:
    https://github.com/ehn-dcc-development/dgc-business-rules/blob/main/certlogic/certlogic-kotlin/src/main/kotlin/eu/ehn/dcc/certlogic/internals.kt

    Modifications Copyright (c) 2022 SAP SE or an SAP affiliate company.
*/

package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.error.argError

internal val JsonNode.isTruthy
    get() = isValueTruthy(this)

internal fun isValueTruthy(value: JsonNode) = when (value) {
    is BooleanNode -> value == BooleanNode.TRUE
    is TextNode -> value.textValue().isNotEmpty()
    is NumericNode -> value.doubleValue().let { !it.isNaN() && it != 0.0 }
    is ArrayNode -> value.size() > 0
    is ObjectNode -> value.size() > 0
    else -> false
}

internal val JsonNode.isFalsy
    get() = isValueFalsy(this)

internal fun isValueFalsy(value: JsonNode): Boolean = when (value) {
    is BooleanNode -> value == BooleanNode.FALSE
    is NullNode -> true
    is TextNode -> value.textValue().isEmpty()
    is NumericNode -> value.doubleValue().let { it.isNaN() || it == 0.0 }
    is ArrayNode -> value.size() == 0
    is ObjectNode -> value.size() == 0
    else -> false
}

internal fun <T : Comparable<T>> compare(operator: String, args: List<T>): Boolean =
    when (args.size) {
        2 -> intCompare(operator, args[0].compareTo(args[1]), 0)
        3 -> intCompare(operator, args[0].compareTo(args[1]), 0) && intCompare(
            operator,
            args[1].compareTo(args[2]),
            0
        )
        else -> argError("invalid number of operands to a \"$operator\" operation")
    }

internal fun intCompare(operator: String, l: Int, r: Int): Boolean =
    when (operator) {
        "<" -> l < r
        ">" -> l > r
        "<=" -> l <= r
        ">=" -> l >= r
        else -> argError("unhandled comparison operator \"$operator\"")
    }

internal const val OPTIONAL_PREFIX = "URN:UVCI:"
