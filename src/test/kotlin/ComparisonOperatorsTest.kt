
import com.fasterxml.jackson.databind.node.BooleanNode
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ComparisonOperatorsTest {

    @Test
    fun `Kotest assertion test`() {
        BooleanNode.valueOf(true) shouldBe BooleanNode.FALSE
    }

    @Test
    fun `test operator '=='`() = assertSoftly {
        """{ "==" : [ 1, 1 ] } """.evaluateJson("{}").booleanValue() shouldBe true
        """{ "==" : [ 1, "1" ] } """.evaluateJson("{}").booleanValue() shouldBe true
        """{ "==" : [ 1, 2 ] } """.evaluateJson("{}").booleanValue() shouldBe false
    }

    @Test
    fun `test operator '==='`() = assertSoftly {
        """{ "===" : [ 1, 1 ] } """.evaluateJson("{}").booleanValue() shouldBe true
        """{ "===" : [ 1, "1" ] } """.evaluateJson("{}").booleanValue() shouldBe false
        """{ "===" : [ 1, 2 ] } """.evaluateJson("{}").booleanValue() shouldBe false
    }

    @Test
    fun `test operator '!='`() = assertSoftly {
        """{ "!=" : [ 1, 1 ] } """.evaluateJson("{}").booleanValue() shouldBe false
        """{ "!=" : [ 1, "1" ] } """.evaluateJson("{}").booleanValue() shouldBe false
        """{ "!=" : [ 1, 2 ] } """.evaluateJson("{}").booleanValue() shouldBe true
    }

    @Test
    fun `test operator '!=='`() = assertSoftly {
        """{ "!==" : [ 1, 1 ] } """.evaluateJson("{}").booleanValue() shouldBe false
        """{ "!==" : [ 1, "1" ] } """.evaluateJson("{}").booleanValue() shouldBe true
        """{ "!==" : [ 1, 2 ] } """.evaluateJson("{}").booleanValue() shouldBe true
    }

    @Test
    fun `test operator 'and'`() = assertSoftly {
        """{ "and" : [ null, true ] }""".evaluateJson("{}").isNull shouldBe true
        """{ "and" : [ false ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{ "and" : [ true ] }""".evaluateJson("{}").booleanValue() shouldBe true
        """{ "and" : [ 1, 3 ] }""".evaluateJson("{}").intValue() shouldBe 3
        """{ "and" : [ 3, false ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{ "and" : [ false, 3 ] }""".evaluateJson("{}").booleanValue() shouldBe false
    }

    @Test
    fun `'in' should be able to handle nulls`() {

        val logic = """
        {
            "in" : [ {
                "var" : "x"
                }, 
                {
                    "var" : "y"
                } 
            ]
        }"""

        logic.evaluateJson("""null""").booleanValue() shouldBe false
        logic.evaluateJson("""{ "x" : null }""").booleanValue() shouldBe false
        logic.evaluateJson("""{ "x" : null, "y" : null}""").booleanValue() shouldBe false
    }

    @Test
    fun `test operator '!'`() = assertSoftly {
        """{"!" : true }""".evaluateJson("{}").booleanValue() shouldBe false
        """{"!" : false }""".evaluateJson("{}").booleanValue() shouldBe true
        """{"!" : [ "" ] }""".evaluateJson("{}").booleanValue() shouldBe true
        """{"!" : [ "0" ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{"!" : [ 0 ] }""".evaluateJson("{}").booleanValue() shouldBe true
        """{"!" : [ [ ] ] }""".evaluateJson("{}").booleanValue() shouldBe true
    }

    @Test
    fun `test 'greater than' operator`() = assertSoftly {
        """{">" : [ 2, 1 ] }""".evaluateJson("{}").booleanValue() shouldBe true
        """{">" : [ 1, 1 ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{">" : [ 1, 2 ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{">" : [ "2", 1 ] }""".evaluateJson("{}").booleanValue() shouldBe true
    }
}