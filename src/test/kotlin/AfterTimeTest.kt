import com.fasterxml.jackson.databind.node.LongNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.evaluateAfter
import de.rki.jfn.evaluateNotAfter
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class AfterTimeTest {

    @Test
    fun `after is false`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-11T20:29:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate))
        assertEquals(false, evaluateAfter(arguments).booleanValue())
    }

    @Test
    fun `after is true`() {
        val firstDate = "2013-08-11T21:22:04.51+01:00"
        val secondDate = "2013-08-11T20:20:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate))
        assertEquals(true, evaluateAfter(arguments).booleanValue())
    }

    @Test
    fun `not after is false`() {
        val firstDate = "2013-08-11T19:22:04.51+01:00"
        val secondDate = "2013-08-11T17:22:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate))
        assertEquals(false, evaluateNotAfter(arguments).booleanValue())
    }

    @Test
    fun `not after is true`() {
        val firstDate = "2013-08-11T20:20:04.51+01:00"
        val secondDate = "2013-08-11T20:20:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate))
        assertEquals(true, evaluateNotAfter(arguments).booleanValue())
    }

    @Test
    fun `wrong argument type raises exception`() {
        val firstDate = 126837738737
        val secondDate = 126837738789
        val arguments = listOf(LongNode(firstDate), LongNode(secondDate))
        assertFailsWith<IllegalArgumentException>(
            message = "wrong type of arguments"
        ) { evaluateNotAfter(arguments).booleanValue() }
    }

    @Test
    fun `wrong date raises exception`() {
        val firstDate = "126837738737"
        val secondDate = "126837738789"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate))
        assertFailsWith<IllegalArgumentException>(
            message = "wrong date format"
        ) { evaluateNotAfter(arguments).booleanValue() }
    }

    @Test
    fun `supports date only`() {
        val firstDate = "2013-08-11" // resolves to 2013-08-11T00:00:00.00+00:00
        val secondDate = "2013-08-11T00:00:04.51+02:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate))
        assertEquals(true, evaluateAfter(arguments).booleanValue())
    }

    @Test
    fun `supports date only 2`() {
        val firstDate = "2013-08-11" // resolves to 2013-08-11T00:00:00.00+00:00
        val secondDate = "2013-08-11T00:00:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate))
        assertEquals(false, evaluateAfter(arguments).booleanValue())
    }
}
