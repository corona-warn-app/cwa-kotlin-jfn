package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.evaluateLogic

enum class StringOperator : Operator {
    Split {
        override val operator = "split"
        override fun invoke(args: ArrayNode, data: JsonNode): JsonNode {
            val scopedString = evaluateLogic(args[0], data)
            val scopedSeparator = evaluateLogic(args[1], data)

            if (isInvalidType(scopedString))
                return ArrayNode(null, emptyList())
            val initialString = scopedString.asText()

            if (isInvalidType(scopedSeparator))
                return ArrayNode(null, listOf(TextNode(initialString)))
            val separator = scopedSeparator.asText()

            val textNodes =
                initialString.split(separator).filter { it.isNotEmpty() }.map { v -> TextNode(v) }
            return ArrayNode(null, textNodes)
        }
    },

    ReplaceAll {
        override val operator = "replaceAll"
        override fun invoke(args: ArrayNode, data: JsonNode): TextNode {
            val scopedString = evaluateLogic(args[0], data)
            val scopedOldValue = evaluateLogic(args[1], data)
            val scopedNewValue = evaluateLogic(args[2], data)

            if (isInvalidType(scopedString)) return TextNode("")
            val initialString = scopedString.asText()

            if (isInvalidType(scopedOldValue)) return TextNode(initialString)
            val oldValue = scopedOldValue.asText()

            if (isInvalidType(scopedNewValue)) return TextNode(initialString)
            val newValue = scopedNewValue.asText()

            return TextNode(initialString.replace(oldValue, newValue))
        }
    },

    Concatenate {
        override val operator = "concatenate"
        override fun invoke(args: ArrayNode, data: JsonNode): TextNode {
            val scopedArguments: List<JsonNode> = args.map { arg -> evaluateLogic(arg, data) }
            val stringBuilder = StringBuilder()
            for (arg in scopedArguments) {
                stringBuilder.append(if (!isInvalidType(arg)) arg.asText() else "")
            }
            return TextNode(stringBuilder.toString())
        }
    },

    Trim {
        override val operator = "trim"
        override fun invoke(args: ArrayNode, data: JsonNode): TextNode {
            val scopedString = evaluateLogic(args[0], data)
            if (isInvalidType(scopedString)) return TextNode("")
            return TextNode(scopedString.asText().trim())
        }
    },

    ToUpperCase {
        override val operator = "toUpperCase"
        override fun invoke(args: ArrayNode, data: JsonNode): TextNode {
            val scopedString = evaluateLogic(args[0], data)
            if (isInvalidType(scopedString)) return TextNode("")
            return TextNode(scopedString.asText().uppercase())
        }
    },

    ToLowerCase {
        override val operator = "toLowerCase"
        override fun invoke(args: ArrayNode, data: JsonNode): TextNode {
            val scopedString = evaluateLogic(args[0], data)
            if (isInvalidType(scopedString)) return TextNode("")
            return TextNode(scopedString.asText().lowercase())
        }
    },

    /**
     *  Substring of the string.
     *
     *  Index and offset could be negative. This behaviour is not implemented by Kotlin
     *  hence these arguments must be cleaned.
     */
    Substring {
        override val operator = "substr"
        override fun invoke(args: ArrayNode, data: JsonNode): TextNode {
            val scopedString = evaluateLogic(args[0], data)
            val scopedIndex = evaluateLogic(args[1], data)
            val scopedOffset = if (args.count() > 2) evaluateLogic(args[2], data) else null

            if (isInvalidType(scopedString)) return TextNode("")
            val initialString = scopedString.asText()

            if (scopedIndex.isNull) {
                throw IllegalArgumentException("Index must not be null")
            }
            if (!scopedIndex.isInt) {
                throw IllegalArgumentException("Index type must be integer")
            }

            val index = scopedIndex.asInt()
            val cleanedIndex = if (index < 0) initialString.length + index else index

            return if (scopedOffset != null && !scopedOffset.isNull) {
                if (!scopedOffset.isInt) {
                    throw IllegalArgumentException("Offset type must be integer")
                }
                val offset = scopedOffset.asInt()
                val cleanedOffset = if (offset < 0) {
                    initialString.length + offset
                } else {
                    cleanedIndex + offset
                }
                TextNode(initialString.substring(cleanedIndex, cleanedOffset))
            } else {
                TextNode(initialString.substring(cleanedIndex))
            }
        }
    };

    /** All types except Array, Object and Null are treated as String  */
    val isInvalidType: (JsonNode) -> Boolean =
        { arg: JsonNode -> (arg.isArray || arg.isObject || arg.isNull) }

    companion object : OperatorSet {
        override val operators: Set<Operator> get() = values().toSet()
    }
}
