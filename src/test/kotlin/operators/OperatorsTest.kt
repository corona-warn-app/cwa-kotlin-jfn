package operators

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import de.rki.jfn.JsonFunctions
import de.rki.jfn.operators.AccessingDataOperator
import de.rki.jfn.operators.ArrayOperator
import de.rki.jfn.operators.ComparisonOperator
import de.rki.jfn.operators.ControlFlowOperator
import de.rki.jfn.operators.ExtractionOperator
import de.rki.jfn.operators.MathOperator
import de.rki.jfn.operators.Operators
import de.rki.jfn.operators.StringOperator
import de.rki.jfn.operators.TimeOperator
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class OperatorsTest {

    @Test
    fun `Unsupported operation`() {
        val jsonNode = JsonNodeFactory.instance.arrayNode()
        val engine = JsonFunctions()
        assertThrows<IllegalStateException> {
            Operators("", engine, jsonNode, jsonNode)
        }.printStackTrace()

        assertThrows<IllegalStateException> {
            Operators("swap", engine, jsonNode, jsonNode)
        }.printStackTrace()
    }

    @Test
    fun `Contains all operators`() {
        Operators.operators.containsAll(ArrayOperator.operators) shouldBe true
        Operators.operators.containsAll(ComparisonOperator.operators) shouldBe true
        Operators.operators.containsAll(MathOperator.operators) shouldBe true
        Operators.operators.containsAll(TimeOperator.operators) shouldBe true
        Operators.operators.containsAll(StringOperator.operators) shouldBe true
        Operators.operators.containsAll(ControlFlowOperator.operators) shouldBe true
        Operators.operators.containsAll(AccessingDataOperator.operators) shouldBe true
        Operators.operators.containsAll(ExtractionOperator.operators) shouldBe true
    }
}
