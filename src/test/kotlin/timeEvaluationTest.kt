import com.fasterxml.jackson.databind.node.*
import de.rki.jfn.evaluateDiffTime
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class DiffTimeTests {

    @Test
    fun `diff time in hours happy path`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-11T20:29:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate), TextNode("hour") )
        assertEquals(4, evaluateDiffTime(arguments).intValue())
    }

    @Test
    fun `diff time in seconds happy path`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-11T17:22:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate), TextNode("second") )
        assertEquals(60*60, evaluateDiffTime(arguments).intValue())
    }

    @Test
    fun `diff time in days happy path`() {
        val firstDate = "2013-08-11T17:22:04.51+01:00"
        val secondDate = "2013-08-14T16:22:04.51+00:00"
        val arguments = listOf(TextNode(firstDate), TextNode(secondDate), TextNode("day") )
        assertEquals(3, evaluateDiffTime(arguments).intValue())
    }
}