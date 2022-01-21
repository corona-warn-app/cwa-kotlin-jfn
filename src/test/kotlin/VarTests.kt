import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class VarTests {

    @Test
    fun `var should return data on empty array`() {
        """
        {
            "var" : [ ]
        }
        """.evaluateJson("1") shouldBe IntNode.valueOf(1)
    }

    @Test
    fun `var should return data on empty string`() {
        """
        {
            "var" : ""
        }
        """.evaluateJson("2") shouldBe IntNode.valueOf(2)
    }

    @Test
    fun `var should return last element of array if size of array is greater than 1`() {
        """
        {
            "var" : [1,2]
        }
        """.evaluateJson("{}") shouldBe IntNode.valueOf(2)

        """
        {
            "var" : [1,2,3]
        }
        """.evaluateJson("{}") shouldBe IntNode.valueOf(3)
    }

    @Test
    fun `var should return data on null`() {
        """
        {
            "var" : null
        }
        """.evaluateJson("1") shouldBe IntNode.valueOf(1)
    }

    @Test
    fun `var should be able to declare operations`() {
        val logic = """
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

        logic.evaluateJson("""{"temp" : 99}""") shouldBe BooleanNode.TRUE
        logic.evaluateJson("""{"temp" : 100}""") shouldBe BooleanNode.FALSE
    }

    @Test
    fun `properties within objects should be assignable to var`() = assertSoftly {
        """
        {
            "var": "pie.filling"
        }
        """.evaluateJson("""{ "pie": { "filling": "apple" } } """).textValue() shouldBe "apple"

        """
        {
          "var" : [ {
            "?:" : [ {
              "<" : [ {
                "var" : "temp"
              }, 110 ]
            }, "pie.filling", "pie.eta" ]
          } ]
        }""".evaluateJson(
            data = """
            { 
                "temp": 100, 
                "pie": 
                { 
                    "filling": "apple", 
                    "eta": "60s" 
                } 
            }
            """
        ).textValue() shouldBe "apple"
    }
}
