package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.JsonFunctions
import de.rki.jfn.error.argError

internal enum class StringOperator : Operator {
    Split {
        override val operator = "split"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            if (args.size() < 2) {
                argError(
                    "an \"$operator\"  operation must have at least 2 operands"
                )
            }

            val scopedString = jfn.evaluate(args[0], data)
            val scopedSeparator = jfn.evaluate(args[1], data)
            val resultArrayNode = JsonNodeFactory.instance.arrayNode()

            if (isInvalidType(scopedString)) {
                return resultArrayNode
            }
            val initialString = scopedString.asText()

            if (isInvalidType(scopedSeparator)) {
                return ArrayNode(null, listOf(TextNode(initialString)))
            }
            val separator = scopedSeparator.asText()

            val textNodes =
                initialString.split(separator).filter { it.isNotEmpty() }.map { v -> TextNode(v) }
            resultArrayNode.addAll(textNodes)
            return resultArrayNode
        }
    },

    ReplaceAll {
        override val operator = "replaceAll"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): TextNode {
            if (args.size() !in 2..3) {
                argError(
                    "an operation with operator \"$operator\" must have 2 or 3 operands"
                )
            }

            val scopedString = jfn.evaluate(args[0], data)
            val scopedOldValue = jfn.evaluate(args[1], data)
            val scopedNewValue = jfn.evaluate(args[2], data)

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

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): TextNode {
            if (args.size() < 2) {
                argError(
                    "an \"$operator\"  operation must have at least 2 operands"
                )
            }

            val scopedArguments = jfn.evaluate(args, data)
            val stringBuilder = StringBuilder()
            for (arg in scopedArguments) {
                stringBuilder.append(if (!isInvalidType(arg)) arg.asText() else "")
            }
            return TextNode(stringBuilder.toString())
        }
    },

    Trim {
        override val operator = "trim"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): TextNode {
            val scopedString = jfn.evaluate(args[0], data)
            if (isInvalidType(scopedString)) return TextNode("")
            return TextNode(scopedString.asText().trim())
        }
    },

    ToUpperCase {
        override val operator = "toUpperCase"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): TextNode {
            if (args.size() > 1) argError("an \"$operator\"  operation must have 1 operand")
            val scopedString = jfn.evaluate(args[0], data)
            if (isInvalidType(scopedString)) return TextNode("")
            return TextNode(scopedString.asText().uppercase())
        }
    },

    ToLowerCase {
        override val operator = "toLowerCase"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): TextNode {
            if (args.size() > 1) argError("an \"$operator\"  operation must have 1 operand")
            val scopedString = jfn.evaluate(args[0], data)
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

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): TextNode {
            if (args.size() !in 2..3) {
                argError(
                    "an operation with operator \"$operator\" must have 2 or 3 operands"
                )
            }

            val scopedString = jfn.evaluate(args[0], data)
            val scopedIndex = jfn.evaluate(args[1], data)
            val scopedOffset = if (args.count() > 2) jfn.evaluate(args[2], data) else null

            if (isInvalidType(scopedString)) return TextNode("")
            val initialString = scopedString.asText()

            if (scopedIndex.isNull) argError("Index must not be null")
            if (!scopedIndex.isInt) argError("Index type must be integer")

            val index = scopedIndex.asInt()
            val cleanedIndex = if (index < 0) initialString.length + index else index
            if (cleanedIndex >= initialString.length || cleanedIndex < 0) {
                argError(
                    "Incorrect index. For this string the index should be in " +
                        "range from ${-initialString.length} to ${initialString.length}"
                )
            }

            return if (scopedOffset != null && !scopedOffset.isNull) {
                if (!scopedOffset.isInt) argError("Offset type must be integer")
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
