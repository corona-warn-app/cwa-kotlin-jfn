package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode

interface JsonFunctions {

    /**
     * Register a function with its name and a JSON definition
     * @param name [String] - the name of the function
     * @param descriptor [JsonNode] - the function descriptor as a JSON object
     *
     * @throws [IllegalArgumentException]
     */
    fun registerFunction(
        name: String,
        descriptor: JsonNode
    )

    /**
     * Evaluate a previously registered function against a set of parameters
     * @param name [String] - the name of the function
     * @param parameters [JsonNode] - the function parameters as a JSON object
     * @return [JsonNode] any value - the result of the function as JSON primitive types
     * (string, number, boolean, or null) or a JSON structured types (object or array)
     *
     * @throws [IllegalArgumentException], [UnsupportedOperationException]
     */
    fun evaluateFunction(
        name: String,
        parameters: JsonNode
    ): JsonNode

    /**
     * Return true if any of the following applies:
     *  - value is a boolean and true
     *  - value is a string and not empty (without trimming value)
     *  - value is a number and not 0 (or any decimal representation of 0)
     *  - value is an array with at least one element
     *  - value is an object with at least one key
     *  - In all other cases, it shall return false.
     * @param value as JSON type (string, number, boolean, object, array, null)
     * @return [Boolean]
     */
    fun isTruthy(value: JsonNode): Boolean

    /**
     * Recursively traverse logic and any JsonFunctions operation that is encountered against data.
     * @param logic [JsonNode] as JSON type (string, number, boolean, object, array, null)
     * @param data [JsonNode] as JSON type (string, number, boolean, object, array, null)
     * @return [JsonNode] a JSON type (string, number, boolean, object, array, null).
     */
    fun evaluate(logic: JsonNode, data: JsonNode): JsonNode

    /**
     * Returns JsonFunctionDescriptor by name if function is already registered
     * @param name [String] function name
     * @return [JsonNode] Function descriptor
     */
    fun getDescriptor(name: String): JsonNode
}
