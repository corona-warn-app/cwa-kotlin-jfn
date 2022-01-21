package de.rki.jfn.operators

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.JsonFunctions
import de.rki.jfn.OPTIONAL_PREFIX
import de.rki.jfn.error.argError
import de.rki.jfn.evaluateLogic

enum class ExtractionOperator : Operator {

    ExtractFromUVCI {
        override val operator = "extractFromUVCI"

        override fun invoke(jfn: JsonFunctions, args: JsonNode, data: JsonNode): JsonNode {
            val operand = args[0]
            val index = args[1]

            val evalOperand = evaluateLogic(jfn, operand, data)
            if (!(evalOperand is NullNode || evalOperand is TextNode)) {
                argError("\"UVCI\" argument (#1) of \"extractFromUVCI\" must be either a string or null")
            }
            if (index !is IntNode) {
                argError("\"index\" argument (#2) of \"extractFromUVCI\" must be an integer")
            }
            val result = extractFromUVCI(
                if (evalOperand is TextNode) evalOperand.asText() else null,
                index.intValue()
            )
            return if (result == null) NullNode.instance else TextNode.valueOf(result)
        }

    };

    companion object : OperatorSet {
        override val operators: Set<Operator> get() = values().toSet()
    }
}

internal fun extractFromUVCI(uvci: String?, index: Int): String? {
    if (uvci == null || index < 0) {
        return null
    }
    val uvciWithoutPrefix =
        if (uvci.startsWith(OPTIONAL_PREFIX)) uvci.substring(OPTIONAL_PREFIX.length) else uvci
    val fragments = uvciWithoutPrefix.split(Regex("[/#:]"))
    return if (index < fragments.size) fragments[index] else null
}
