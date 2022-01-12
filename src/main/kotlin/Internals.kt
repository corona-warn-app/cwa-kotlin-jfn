package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

internal fun isValueTruthy(value: JsonNode) = when (value) {
    is BooleanNode -> value == BooleanNode.TRUE
    is TextNode -> value.textValue().isNotEmpty()
    is IntNode -> value.intValue() != 0
    is ArrayNode -> value.size() > 0
    is ObjectNode -> value.size() > 0
    else -> false
}

internal fun isValueFalsy(value: JsonNode): Boolean = when (value) {
    is BooleanNode -> value == BooleanNode.FALSE
    is NullNode -> true
    is TextNode -> value.textValue().isEmpty()
    is IntNode -> value.intValue() == 0
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
        else -> throw RuntimeException(
            "invalid number of operands to a \"$operator\" operation"
        )
    }

internal fun intCompare(operator: String, l: Int, r: Int): Boolean =
    when (operator) {
        "<" -> l < r
        ">" -> l > r
        "<=" -> l <= r
        ">=" -> l >= r
        else -> throw RuntimeException("unhandled comparison operator \"$operator\"")
    }

internal const val OPTIONAL_PREFIX = "URN:UVCI:"
internal fun extractFromUVCI(uvci: String?, index: Int): String? {
    if (uvci == null || index < 0) {
        return null
    }
    val prefixlessUvci =
        if (uvci.startsWith(OPTIONAL_PREFIX)) uvci.substring(OPTIONAL_PREFIX.length) else uvci
    val fragments = prefixlessUvci.split(Regex("[/#:]"))
    return if (index < fragments.size) fragments[index] else null
}
