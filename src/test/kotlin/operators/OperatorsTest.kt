package operators

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import de.rki.jfn.JsonFunctionsEngine
import de.rki.jfn.operators.Operators
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class OperatorsTest {

    @Test
    fun `Unsupported operation`() {
        val jsonNode = JsonNodeFactory.instance.arrayNode()
        val engine = JsonFunctionsEngine()
        assertThrows<IllegalStateException> {
            Operators("", engine, jsonNode, jsonNode)
        }

        assertThrows<IllegalStateException> {
            Operators("swap", engine, jsonNode, jsonNode)
        }
    }
}
