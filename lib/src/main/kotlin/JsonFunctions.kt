package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode

interface JsonFunctions {

    fun registerFunction(
        name: String,
        descriptor: JsonNode
    )

    fun evaluateFunction(
        name: String,
        parameters: JsonNode
    ): Any?

    fun isTruthy(value: JsonNode): Boolean

    fun evaluate(logic: JsonNode, data: JsonNode): JsonNode
}