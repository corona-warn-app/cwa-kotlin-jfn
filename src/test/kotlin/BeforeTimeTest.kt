import com.fasterxml.jackson.databind.node.LongNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.evaluateBefore
import de.rki.jfn.evaluateNotBefore
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class BeforeTimeTest {

    @Test
    fun `before is false`() {
        val firstDate = "2013-08-11T20:20:04.51+00:00"
        val secondDate = "2013-08-11T20:20:04.51+01:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate))
        assertEquals(false, evaluateBefore(arguments).booleanValue())
    }

    @Test
    fun `before is true`() {
        val firstDate = "2013-08-11T20:22:04.51+01:00"
        val secondDate = "2013-08-11T20:22:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate))
        assertEquals(true, evaluateBefore(arguments).booleanValue())
    }

    @Test
    fun `not before is false`() {
        val firstDate = "2013-08-11T20:22:04.51+01:00"
        val secondDate = "2013-08-11T20:22:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate))
        assertEquals(false, evaluateNotBefore(arguments).booleanValue())
    }

    @Test
    fun `not before is true`() {
        val firstDate = "2013-08-11T20:20:04.51+00:00"
        val secondDate = "2013-08-11T20:20:04.51+01:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate))
        assertEquals(true, evaluateNotBefore(arguments).booleanValue())
    }

    @Test
    fun `wrong argument returns true`() {
        val firstDate = 126837738737
        val secondDate = 126837738789
        val arguments = listOf(LongNode(firstDate), LongNode(secondDate))
        assertEquals(true, evaluateNotBefore(arguments).booleanValue())
    }

    @Test
    fun `wrong date raises exception`() {
        val firstDate = "126837738737"
        val secondDate = "126837738789"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate))
        assertFailsWith<IllegalArgumentException>(
            message = "wrong date format"
        ) { evaluateBefore(arguments).booleanValue() }
    }

    @Test
    fun `supports date only`() {
        val firstDate = "2013-08-11" // resolves to 2013-08-11T00:00:00.00+00:00
        val secondDate = "2013-08-11T12:00:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate))
        assertEquals(true, evaluateBefore(arguments).booleanValue())
    }
}
