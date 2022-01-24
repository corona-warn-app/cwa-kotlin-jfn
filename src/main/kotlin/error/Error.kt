package de.rki.jfn.error

/**
 * @param message [Any]
 * @throws [IllegalArgumentException] with message
 */
fun argError(message: Any): Nothing = throw IllegalArgumentException(message.toString())

/**
 * Function isn't registered yet error
 */
class NoSuchFunctionException :
    IllegalStateException("No such function was registered in the engine")
