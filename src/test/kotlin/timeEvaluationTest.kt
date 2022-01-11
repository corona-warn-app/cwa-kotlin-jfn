import com.fasterxml.jackson.databind.node.*
import de.rki.jfn.evaluateDiffTime
import de.rki.jfn.evaluatePlusTime
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class DiffTimeTests {

    @Test
    fun `diff time in hours`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-11T20:29:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate), TextNode("hour") )
        assertEquals(4, evaluateDiffTime(arguments).intValue())
    }

    @Test
    fun `diff time in seconds`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-11T17:22:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate), TextNode("second") )
        assertEquals(60*60, evaluateDiffTime(arguments).intValue())
    }

    @Test
    fun `diff time in days`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-14T16:34:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate), TextNode("day") )
        assertEquals(3, evaluateDiffTime(arguments).intValue())
    }

    @Test
    fun `diff time in minutes`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-11T20:29:06.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate), TextNode("minute") )
        assertEquals(4*60 + 7, evaluateDiffTime(arguments).intValue())
    }

    @Test
    fun `diff time in months`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-11T17:22:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate), TextNode("month") )
        assertEquals(0, evaluateDiffTime(arguments).intValue())
    }

    @Test
    fun `diff time in years`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2019-08-10T16:22:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate), TextNode("year") )
        assertEquals(5, evaluateDiffTime(arguments).intValue())
    }

    @Test
    fun `wrong number of arguments raises exception`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val arguments = listOf(TextNode(firstDate), TextNode("year"))
        assertFailsWith<IllegalArgumentException>(
            message = "wrong number of arguments"
        ) { evaluateDiffTime(arguments) }
    }

    @Test
    fun `wrong order of arguments raises exception`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val arguments = listOf(TextNode(firstDate), TextNode("year"),  IntNode(4),)
        assertFailsWith<IllegalArgumentException>(
            message = "wrong order of arguments"
        ) { evaluateDiffTime(arguments) }
    }

    @Test
    fun `wrong type of arguments raises exception`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val arguments = listOf(TextNode(firstDate), TextNode("year"),  TextNode("4"),)
        assertFailsWith<IllegalArgumentException>(
            message = "wrong type of arguments"
        ) { evaluateDiffTime(arguments) }
    }

    @Test
    fun `wrong date raises exception`() {
        val firstDate = "1.1.2022 16:30:15"
        val secondDate = "2019-08-10T16:22:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate), TextNode("year") )
        assertFailsWith<IllegalArgumentException>(
            message = "wrong type of arguments"
        ) { evaluateDiffTime(arguments) }
    }
}

internal class PlusTimeTests {
    @Test
    fun `plus time in hours`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-11T21:22:04.510+01:00"
        val arguments = listOf(TextNode(firstDate), IntNode(4), TextNode("hour"))
        assertEquals(secondDate, evaluatePlusTime(arguments).asText())
    }

    @Test
    fun `plus time in minutes`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-11T17:26:04.510+01:00"
        val arguments = listOf(TextNode(firstDate), IntNode(4), TextNode("minute"))
        assertEquals(secondDate, evaluatePlusTime(arguments).asText())
    }

    @Test
    fun `plus time in seconds`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-11T17:22:08.510+01:00"
        val arguments = listOf(TextNode(firstDate), IntNode(4), TextNode("second"))
        assertEquals(secondDate, evaluatePlusTime(arguments).asText())
    }

    @Test
    fun `plus time in days`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-15T17:22:04.510+01:00"
        val arguments = listOf(TextNode(firstDate), IntNode(4), TextNode("day"))
        assertEquals(secondDate, evaluatePlusTime(arguments).asText())
    }

    @Test
    fun `plus time in months`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-12-11T17:22:04.510+01:00"
        val arguments = listOf(TextNode(firstDate), IntNode(4), TextNode("month"))
        assertEquals(secondDate, evaluatePlusTime(arguments).asText())
    }

    @Test
    fun `plus time in years`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2017-08-11T17:22:04.510+01:00"
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
        val arguments = listOf(TextNode(firstDate), TextNode("year"),  IntNode(4),)
        assertFailsWith<IllegalArgumentException>(
            message = "wrong order of arguments"
        ) { evaluatePlusTime(arguments) }
    }

    @Test
    fun `wrong type of arguments raises exception`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val arguments = listOf(TextNode(firstDate), TextNode("year"),  TextNode("4"),)
        assertFailsWith<IllegalArgumentException>(
            message = "wrong type of arguments"
        ) { evaluatePlusTime(arguments) }
    }
}