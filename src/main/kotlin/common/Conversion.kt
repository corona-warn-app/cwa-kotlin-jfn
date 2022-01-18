package de.rki.jfn.common

import com.fasterxml.jackson.databind.node.DoubleNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.LongNode
import com.fasterxml.jackson.databind.node.NumericNode

/** Converts an [Int] to an [IntNode] */
internal fun Int.toIntNode() = IntNode.valueOf(this)

/** Converts a [Double] to a [DoubleNode] */
internal fun Double.toDoubleNode() = DoubleNode.valueOf(this)

/** Converts a [Long] to a [LongNode]*/
internal fun Long.toLongNode() = LongNode.valueOf(this)

/**
 * Converts a [Double] to a [NumericNode] after stripping any trailing zero.
 */
internal fun Double.toNumericNode(): NumericNode {
    val t = "2147483647"
    println("t as int=${t.toIntOrNull()}")
    println("t as long=${t.toLongOrNull()}")
    val plainNumber = toBigDecimal().stripTrailingZeros().toPlainString()
    return with(plainNumber) { toIntOrNull()?.toIntNode() ?: toLongOrNull()?.toLongNode() }
        ?: toDoubleNode()
}