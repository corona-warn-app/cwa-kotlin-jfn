import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.rki.jfn.JsonFunctionsEngine
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VarTests {

    private val engine = JsonFunctionsEngine()

    @Test
    fun `var should return data on empty array`() {
        val logic = jacksonObjectMapper().readTree(
            """
            {
                "var" : [ ]
            }"""
        )

        val data = jacksonObjectMapper().readTree("1")
        assertEquals(IntNode.valueOf(1), engine.evaluate(logic, data))
    }

    @Test
    fun `var should return data on blank string`() {
        val logic = jacksonObjectMapper().readTree(
            """
            {
                "var" : ""
            }"""
        )

        val data = jacksonObjectMapper().readTree("2")
        assertEquals(IntNode.valueOf(2), engine.evaluate(logic, data))
    }

    @Test
    fun `var should last element of array if size of array is greater than 1`() {
        val logic2elements = jacksonObjectMapper().readTree(
            """
            {
                "var" : [1,2]
            }"""
        )

        val data = jacksonObjectMapper().readTree("{}")
        assertEquals(IntNode.valueOf(2), engine.evaluate(logic2elements, data))

        val logic3elements = jacksonObjectMapper().readTree(
            """
            {
                "var" : [1,2,3]
            }"""
        )
        assertEquals(IntNode.valueOf(3), engine.evaluate(logic3elements, data))
    }

    @Test
    fun `var should return data on null`() {
        val logic = jacksonObjectMapper().readTree(
            """
            {
                "var" : null
            }"""
        )

        val data = jacksonObjectMapper().readTree("3")
        assertEquals(IntNode.valueOf(3), engine.evaluate(logic, data))
    }

    @Test
    fun `var should be able to declare operations`() {
        val logic = jacksonObjectMapper().readTree(
            """
            {
                "var" : [ 
                    {
                        "<" : [ 
                            {
                                "var" : "temp"
                            }, 
                            100 
                        ]
                    }
                ]
            }"""
        )

        val dataTemp99 = jacksonObjectMapper().readTree("""{"temp" : 99}""")
        assertEquals(BooleanNode.TRUE, engine.evaluate(logic, dataTemp99))

        val dataTemp100 = jacksonObjectMapper().readTree("""{"temp" : 100}""")
        assertEquals(BooleanNode.FALSE, engine.evaluate(logic, dataTemp100))
    }
}
