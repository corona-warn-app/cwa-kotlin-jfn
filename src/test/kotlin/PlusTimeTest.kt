import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.evaluatePlusTime
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class PlusTimeTest {
    @Test
    fun `plus time in hours`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-11T21:22:04+01:00"
        val arguments = listOf(TextNode(firstDate), IntNode(4), TextNode("hour"))
        assertEquals(secondDate, evaluatePlusTime(arguments).asText())
    }

    @Test
    fun `plus time in minutes`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-11T17:26:04+01:00"
        val arguments = listOf(TextNode(firstDate), IntNode(4), TextNode("minute"))
        assertEquals(secondDate, evaluatePlusTime(arguments).asText())
    }

    @Test
    fun `plus time in seconds`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-11T17:22:08+01:00"
        val arguments = listOf(TextNode(firstDate), IntNode(4), TextNode("second"))
        assertEquals(secondDate, evaluatePlusTime(arguments).asText())
    }

    @Test
    fun `plus time in days`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-15T17:22:04+01:00"
        val arguments = listOf(TextNode(firstDate), IntNode(4), TextNode("day"))
        assertEquals(secondDate, evaluatePlusTime(arguments).asText())
    }

    @Test
    fun `plus time in months`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-12-11T17:22:04+01:00"
        val arguments = listOf(TextNode(firstDate), IntNode(4), TextNode("month"))
        assertEquals(secondDate, evaluatePlusTime(arguments).asText())
    }

    @Test
    fun `plus time in years`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2017-08-11T17:22:04+01:00"
        val arguments = listOf(TextNode(firstDate), IntNode(4), TextNode("year"))
        assertEquals(secondDate, evaluatePlusTime(arguments).asText())
    }

    @Test
    fun `wrong number of arguments raises exception`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val arguments = listOf(TextNode(firstDate), TextNode("year"))
        assertFailsWith<IllegalArgumentException>(
            message = "wrong number of arguments"
        ) { evaluatePlusTime(arguments) }
    }

    @Test
    fun `wrong order of arguments raises exception`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val arguments = listOf(TextNode(firstDate), TextNode("year"), IntNode(4))
        assertFailsWith<IllegalArgumentException>(
            message = "wrong order of arguments"
        ) { evaluatePlusTime(arguments) }
    }

    @Test
    fun `wrong type of arguments raises exception`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val arguments = listOf(TextNode(firstDate), TextNode("year"), TextNode("4"))
        assertFailsWith<IllegalArgumentException>(
            message = "wrong type of arguments"
        ) { evaluatePlusTime(arguments) }
    }
}
