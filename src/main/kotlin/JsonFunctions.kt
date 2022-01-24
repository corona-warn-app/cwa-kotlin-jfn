package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import de.rki.jfn.error.NoSuchFunctionException
import de.rki.jfn.error.argError
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class JsonFunctions {

    private val registeredFunctions = mutableMapOf<String, JsonNode>()
    private val nodeFactory = JsonNodeFactory.instance
    private val mutex = Mutex()

    /**
     * Register a function with its name and a JSON definition
     * @param name [String] - the name of the function
     * @param descriptor [JsonNode] - the function descriptor as a JSON object
     *
     * @throws [IllegalArgumentException]
     */
    fun registerFunction(name: String, descriptor: JsonNode) = runBlocking {
        mutex.withLock {
            if (!descriptor.has(PARAMETERS)) {
                argError("descriptor must have a '$PARAMETERS' property!")
            }
            if (descriptor.get(PARAMETERS) !is ArrayNode) {
                argError("'$PARAMETERS' of descriptor must be an array!")
            }

            if (!descriptor.has(LOGIC)) {
                argError("descriptor must have a '$LOGIC' property!")
            }
            if (descriptor.get(LOGIC) !is ArrayNode) {
                argError("'$LOGIC' of descriptor must be an array!")
            }

            registeredFunctions[name] = descriptor
        }
    }

    /**
     * Evaluate a previously registered function against a set of parameters
     * @param name [String] - the name of the function
     * @param parameters [JsonNode] - the function parameters as a JSON object
     * @return [JsonNode] any value - the result of the function as JSON primitive types
     * (string, number, boolean, or null) or a JSON structured types (object or array)
     *
     * @throws [IllegalArgumentException], [UnsupportedOperationException]
     */
    fun evaluateFunction(name: String, parameters: JsonNode): JsonNode = runBlocking {
        mutex.withLock {
            val (logic, params) = functionLogicParamsPair(name)
            evaluate(
                logic,
                determineData(params, parameters)
            )
        }
    }

    /**
     * Recursively traverse logic and any JsonFunctions operation that is encountered against data.
     * @param logic [JsonNode] as JSON type (string, number, boolean, object, array, null)
     * @param data [JsonNode] as JSON type (string, number, boolean, object, array, null)
     * @return [JsonNode] a JSON type (string, number, boolean, object, array, null).
     */
    fun evaluate(logic: JsonNode, data: JsonNode): JsonNode {
        return try {
            evaluateLogic(this, logic, data)
        } catch (e: ReturnException) {
            e.data
        }
    }

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
    fun isTruthy(value: JsonNode): Boolean {
        return isValueTruthy(value)
    }

    internal fun determineData(parameters: ArrayNode, input: JsonNode): JsonNode {
        return nodeFactory.objectNode().apply {
            parameters.forEach {
                val propertyName = it.get("name").textValue()
                when {
                    input.has(propertyName) -> set<JsonNode>(propertyName, input[propertyName])
                    it.has("default") -> set<JsonNode>(propertyName, it["default"])
                    else -> set<JsonNode>(propertyName, NullNode.instance)
                }
            }
        }
    }

    /**
     * Returns a pair of a `jsonFunction` (logic and params)
     * @throws NoSuchFunctionException if function was not registered already
     */
    internal fun functionLogicParamsPair(name: String): Pair<ArrayNode, ArrayNode> {
        val functionDescriptor = registeredFunctions[name] ?: throw NoSuchFunctionException()
        val funcLogic = functionDescriptor.get(LOGIC) as ArrayNode
        val funcParams = functionDescriptor.get(PARAMETERS) as ArrayNode
        return funcLogic to funcParams
    }

    companion object {
        private const val PARAMETERS = "parameters"
        private const val LOGIC = "logic"
    }
}
