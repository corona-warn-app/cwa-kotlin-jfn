package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class JsonFunctionsEngine : JsonFunctions {

    private val registeredFunctions = mutableMapOf<String, JsonNode>()
    private val nodeFactory = JsonNodeFactory.instance

    private val parametersPropertyName = "parameters"
    private val logicPropertyName = "logic"

    private val mutex = Mutex()

    override fun registerFunction(name: String, descriptor: JsonNode) = runBlocking {
        mutex.withLock {
            if (!descriptor.has(parametersPropertyName)) {
                throw RuntimeException("descriptor must have a '$parametersPropertyName' property!")
            }
            if (descriptor.get(parametersPropertyName) !is ArrayNode) {
                throw RuntimeException("'$parametersPropertyName' of descriptor must be an array!")
            }

            if (!descriptor.has(logicPropertyName)) {
                throw RuntimeException("descriptor must have a '$logicPropertyName' property!")
            }
            if (descriptor.get(logicPropertyName) !is ArrayNode) {
                throw RuntimeException("'$logicPropertyName' of descriptor must be an array!")
            }

            registeredFunctions[name] = descriptor
        }
    }

    override fun evaluateFunction(name: String, parameters: JsonNode): JsonNode = runBlocking {
        mutex.withLock {
            val functionDescriptor = registeredFunctions[name] ?: throw NoSuchFunctionException()

            val functionDescriptorParameters = functionDescriptor.get(parametersPropertyName) as ArrayNode
            val functionDescriptorLogic = functionDescriptor.get(logicPropertyName) as ArrayNode

            evaluate(functionDescriptorLogic.first(), determineData(functionDescriptorParameters, parameters))
        }
    }

    fun determineData(parameters: ArrayNode, input: JsonNode): JsonNode {
        return nodeFactory.objectNode().apply {
            parameters.forEach {
                val propertyName = it.get("name").textValue()
                when {
                    input.has(propertyName) -> set<JsonNode>(propertyName, input[propertyName])
                    input.has("default") -> set<JsonNode>(propertyName, input["default"])
                }
            }
        }
    }

    override fun evaluate(logic: JsonNode, data: JsonNode): JsonNode {
        return evaluateLogic(logic, data)
    }

    override fun isTruthy(value: JsonNode): Boolean {
        return isValueTruthy(value)
    }
}

class NoSuchFunctionException : Exception("No such function was registered in the engine")
