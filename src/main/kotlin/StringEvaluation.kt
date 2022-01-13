package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.TextNode

internal fun evaluateSplit(arguments: List<JsonNode>): ArrayNode {
    if (isNotValidType(arguments[0])) return ArrayNode(null, emptyList())
    val value = arguments[0].asText()

    if (isNotValidType(arguments[1])) return ArrayNode(null, listOf(TextNode(value)))
    val separator = arguments[1].asText()

    val textNodes = value.split(separator).filter { it.isNotEmpty() }.map { v -> TextNode(v) }
    return ArrayNode(null, textNodes)
}

internal fun evaluateReplaceAll(arguments: List<JsonNode>): TextNode {
    if (isNotValidType(arguments[0])) return TextNode("")
    val initialString = arguments[0].asText()

    if (isNotValidType(arguments[1])) return TextNode(initialString)
    val oldValue = arguments[1].asText()

    if (isNotValidType(arguments[2])) return TextNode(initialString)
    val newValue = arguments[2].asText()

    return TextNode(initialString.replace(oldValue, newValue))
}

internal fun evaluateConcatenate(arguments: List<JsonNode>): TextNode {
    val stringBuilder = StringBuilder()
    for (arg in arguments) {
        stringBuilder.append(if (!isNotValidType(arg)) arg.asText() else "")
    }
    return TextNode(stringBuilder.toString())
}

internal fun evaluateTrim(arguments: List<JsonNode>): TextNode {
    if (isNotValidType(arguments[0])) return TextNode("")
    return TextNode(arguments[0].asText().trim())
}

internal fun evaluateToUpperCase(arguments: List<JsonNode>): TextNode {
    if (isNotValidType(arguments[0])) return TextNode("")
    return TextNode(arguments[0].asText().uppercase())
}

internal fun evaluateToLowerCase(arguments: List<JsonNode>): TextNode {
    if (isNotValidType(arguments[0])) return TextNode("")
    return TextNode(arguments[0].asText().lowercase())
}

/**
 *  Substring of the string.
 *
 *  [0] initialString
 *  [1] index (or start)
 *  [2] offset (or end)
 *
 *  Index and offset could be negative. This behaviour is not implemented by Kotlin
 *  hence these arguments must be cleaned.
 */
internal fun evaluateSubstr(arguments: List<JsonNode>): TextNode {
    if (isNotValidType(arguments[0])) return TextNode("")
    val initialString = arguments[0].asText()

    if (arguments[1].isNull) {
        throw IllegalArgumentException("Index must not be null")
    }
    if (!arguments[1].isInt) {
        throw IllegalArgumentException("Index type must be integer")
    }

    val index = arguments[1].asInt()
    val cleanedIndex = if (index < 0) initialString.length + index else index

    return if (arguments.size > 2 && !arguments[2].isNull) {
        if (!arguments[2].isInt) {
            throw IllegalArgumentException("Offset type must be integer")
        }
        val offset = arguments[2].asInt()
        val cleanedOffset = if (offset < 0) initialString.length + offset else cleanedIndex + offset
        TextNode(initialString.substring(cleanedIndex, cleanedOffset))
    } else {
        TextNode(initialString.substring(cleanedIndex))
    }
}

/** All types except Array, Object and Null are treated as String  */
private val isNotValidType: (JsonNode) -> Boolean =
    { arg: JsonNode -> (arg.isArray || arg.isObject || arg.isNull) }
