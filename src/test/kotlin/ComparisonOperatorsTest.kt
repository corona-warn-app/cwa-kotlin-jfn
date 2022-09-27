
import com.fasterxml.jackson.databind.node.TextNode
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ComparisonOperatorsTest {

    @Test
    fun `test operator '=='`() = assertSoftly {
        """{ "==" : [ 1, 1 ] } """.evaluateJson("{}").booleanValue() shouldBe true
        """{ "==" : [ 1, "1" ] } """.evaluateJson("{}").booleanValue() shouldBe true
        """{ "==" : [ 1, 2 ] } """.evaluateJson("{}").booleanValue() shouldBe false

        """{ "==" : [ "1.0", 1 ] } """.evaluateJson("{}").booleanValue() shouldBe true
        """{ "==" : [ 1.0, "1" ] } """.evaluateJson("{}").booleanValue() shouldBe true
        """{ "==" : [ "1.0", "1" ] } """.evaluateJson("{}").booleanValue() shouldBe false
        """{ "==" : [ 1.0, 1 ] } """.evaluateJson("{}").booleanValue() shouldBe true

        """{ "==" : [ 1.0, "a" ] } """.evaluateJson("{}").booleanValue() shouldBe false
    }

    @Test
    fun `test operator '==='`() = assertSoftly {
        """{ "===" : [ 1, 1 ] } """.evaluateJson("{}").booleanValue() shouldBe true
        """{ "===" : [ 1, "1" ] } """.evaluateJson("{}").booleanValue() shouldBe false
        """{ "===" : [ 1, 2 ] } """.evaluateJson("{}").booleanValue() shouldBe false

        """{ "===" : [ "1.0", 1 ] } """.evaluateJson("{}").booleanValue() shouldBe false
        """{ "===" : [ 1.0, "1" ] } """.evaluateJson("{}").booleanValue() shouldBe false
        """{ "===" : [ "1.0", "1" ] } """.evaluateJson("{}").booleanValue() shouldBe false
        """{ "===" : [ 1.0, 1 ] } """.evaluateJson("{}").booleanValue() shouldBe true
    }

    @Test
    fun `test operator '!='`() = assertSoftly {
        """{ "!=" : [ 1, 1 ] } """.evaluateJson("{}").booleanValue() shouldBe false
        """{ "!=" : [ 1, "1" ] } """.evaluateJson("{}").booleanValue() shouldBe false
        """{ "!=" : [ 1, 2 ] } """.evaluateJson("{}").booleanValue() shouldBe true
        """{ "!=" : [ 1, 1.0 ] } """.evaluateJson("{}").booleanValue() shouldBe false

        """{ "!=" : [ 1, "a" ] } """.evaluateJson("{}").booleanValue() shouldBe true
    }

    @Test
    fun `test operator '!=='`() = assertSoftly {
        """{ "!==" : [ 1, 1 ] } """.evaluateJson("{}").booleanValue() shouldBe false
        """{ "!==" : [ 1, "1" ] } """.evaluateJson("{}").booleanValue() shouldBe true
        """{ "!==" : [ 1, 2 ] } """.evaluateJson("{}").booleanValue() shouldBe true

        """{ "!==" : [ "1.0", 1 ] } """.evaluateJson("{}").booleanValue() shouldBe true
        """{ "!==" : [ 1.0, "1" ] } """.evaluateJson("{}").booleanValue() shouldBe true
        """{ "!==" : [ "1.0", "1" ] } """.evaluateJson("{}").booleanValue() shouldBe true
        """{ "!==" : [ 1.0, 1 ] } """.evaluateJson("{}").booleanValue() shouldBe false
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
    fun `test operator 'in'`() = assertSoftly {
        """{
            "in" : [ "Spring", "Springfield" ]
        }""".evaluateJson("{}").booleanValue() shouldBe true

        """{
            "in" : [ 
                {
                    "var" : ""
                }, 
                [ "foo", "bar" ] 
            ]
        }""".evaluateJson(TextNode.valueOf("foo")).booleanValue() shouldBe true

        """{
            "in" : [ 
                {
                    "var" : ""
                }, 
                [ "foo", "bar" ] 
            ]
        }""".evaluateJson(TextNode.valueOf("bar")).booleanValue() shouldBe true

        """{
            "in" : [ 
                {
                    "var" : ""
                }, 
                [ ] 
            ]
        }""".evaluateJson("").booleanValue() shouldBe false

        """{
             "in" : [ "Bart", [ "Bart", "Homer", "Lisa", "Marge", "Maggie" ] ]
        }""".evaluateJson("{}").booleanValue() shouldBe true
    }

    @Test
    fun `'in' should be able to handle nulls`() = assertSoftly {
        val logic = """
        {
            "in" : [ 
                {
                    "var" : "x"
                }, 
                {
                    "var" : "y"
                } 
            ]
        }"""

        logic.evaluateJson("""null""").booleanValue() shouldBe false
        logic.evaluateJson("""{ }""").booleanValue() shouldBe false
        logic.evaluateJson("""{ "x": null }""").booleanValue() shouldBe false
        logic.evaluateJson("""{ "x": null, "y": null}""").booleanValue() shouldBe false
        logic.evaluateJson("""{ "x": null, "y": []}""").booleanValue() shouldBe false
        logic.evaluateJson("""{ "x": null, "y": [null]}""").booleanValue() shouldBe true
        logic.evaluateJson("""{ "x": "a", "y": ["a","b"]}""").booleanValue() shouldBe true
    }

    @Test
    fun `test operator '!'`() = assertSoftly {
        """{"!": true }""".evaluateJson("{}").booleanValue() shouldBe false
        """{"!": false }""".evaluateJson("{}").booleanValue() shouldBe true
        """{"!": [ "" ] }""".evaluateJson("{}").booleanValue() shouldBe true
        """{"!": [ "0" ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{"!": [ 0 ] }""".evaluateJson("{}").booleanValue() shouldBe true
        """{"!": [ [ ] ] }""".evaluateJson("{}").booleanValue() shouldBe true
    }

    @Test
    fun `test 'greater than' operator`() = assertSoftly {
        """{">" : [ 2, 1 ] }""".evaluateJson("{}").booleanValue() shouldBe true
        """{">" : [ 1, 1 ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{">" : [ 1, 2 ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{">" : [ "2", 1 ] }""".evaluateJson("{}").booleanValue() shouldBe true

        """{">" : [ "2.0", 1 ] }""".evaluateJson("{}").booleanValue() shouldBe true
    }

    @Test
    fun `test 'greater or equals' operator`() = assertSoftly {
        """{">=" : [ 2, 1 ] }""".evaluateJson("{}").booleanValue() shouldBe true
        """{">=" : [ 1, 1 ] }""".evaluateJson("{}").booleanValue() shouldBe true
        """{">=" : [ 1, 1.0 ] }""".evaluateJson("{}").booleanValue() shouldBe true
        """{">=" : [ 1, 2 ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{">=" : [ 1, 2.0 ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{">=" : [ 2.1, 2.0 ] }""".evaluateJson("{}").booleanValue() shouldBe true
        """{">=" : [ "2", 1 ] }""".evaluateJson("{}").booleanValue() shouldBe true
    }

    @Test
    fun `test 'smaller than' operator`() = assertSoftly {
        """{"<" : [ 2, 1 ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{"<" : [ 1, 1 ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{"<" : [ 1, 2 ] }""".evaluateJson("{}").booleanValue() shouldBe true
        """{"<" : [ "2", 1 ] }""".evaluateJson("{}").booleanValue() shouldBe false

        """{"<" : [ "2", 1.0 ] }""".evaluateJson("{}").booleanValue() shouldBe false
    }

    @Test
    fun `test 'smaller or equals' operator`() = assertSoftly {
        """{"<=" : [ 2, 1 ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{"<=" : [ 1, 1 ] }""".evaluateJson("{}").booleanValue() shouldBe true
        """{"<=" : [ 1, 2 ] }""".evaluateJson("{}").booleanValue() shouldBe true
        """{"<=" : [ "2", 1 ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{"<=" : [ "2.0", 1 ] }""".evaluateJson("{}").booleanValue() shouldBe false
        """{"<=" : [ "1.0", 1 ] }""".evaluateJson("{}").booleanValue() shouldBe true
    }
}
