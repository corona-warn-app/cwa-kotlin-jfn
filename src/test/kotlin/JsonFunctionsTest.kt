import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.util.RawValue
import de.rki.jfn.JsonFunctionsEngine
import de.rki.jfn.NoSuchFunctionException
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonFunctionsTest {

    private val nodeFactory = JsonNodeFactory.instance

    @Test
    fun `evaluateFunction() should throw NoSuchFunctionException() when no function was registered before`() {
        JsonFunctionsEngine().run {
            assertThrows<NoSuchFunctionException> {
                evaluateFunction("unregisteredFunctionName", nodeFactory.nullNode())
            }
        }
    }

    @Test
    fun `simpleTest`() {
        JsonFunctionsEngine().run {

            val smaller1Json = """
            {
                "parameters": [], 
                "logic": {
                    "<": [
                        1,
                        1
                    ]
                }
            }
            """.trimIndent()
            val smaller1Node = ObjectMapper().readTree(smaller1Json)
            val emptyNode = ObjectMapper().readTree("{}")

            registerFunction("smallerTest1", smaller1Node)
            val result = evaluateFunction("smallerTest1", emptyNode)

            assertEquals(false, result as Boolean)
        }
    }

    @Test
    fun `determineData() should return proper JsonNode`() {
        JsonFunctionsEngine().run {

            val parametersDefinition =
                """parameters: [{ name: 'key' },{ name: 'lang' }, { name: 'defaultLang', 'default': 'en'}]"""

            val parametersData = "{ key: 'Greeting', lang: 'de' }"

            val expectedData = """{
                key: 'GREETING',
                lang: 'de',
                defaultLang: 'en'
            }"""

            val parameterDefinitionNode = nodeFactory.rawValueNode(RawValue(parametersData))
            val parameterDataNode = nodeFactory.rawValueNode(RawValue(parametersData))
            val data = determineData(parameterDefinitionNode, parameterDataNode)

            assertEquals(
                expectedData,
                data.toString()
            )

        }
    }

    @Test
    fun `examples from TechSpec`() = JsonFunctionsEngine().run {

        val getI18nTextBundleJson = """
            {
              parameters: [
              ],
              logic: [
                {
                  return: [
                    [
                      {
                        key: 'GREETING',
                        texts: [
                          { lang: 'en', text: 'Hello ${'$'}1' },
                          { lang: 'de', text: 'Hallo ${'$'}1' },
                          { lang: 'es', text: '¡${'$'}1, hola ${'$'}1!' },
                        ]
                      }
                    ]
                  ]
                }
              ]
            }
        """.trimIndent()
        val getI18nTextBundleNode = JsonNodeFactory.instance.rawValueNode(RawValue(getI18nTextBundleJson))
        registerFunction("getI18nTextBundle", getI18nTextBundleNode)

        val getI18nTextByKeyJson = """
            {
              parameters: [
                { name: 'key' },
                { name: 'lang' },
                { name: 'defaultLang', 'default': 'en' },
              ],
              logic: [
                {
                  declare: [
                    'textBundle',
                    {
                      call: [
                        'getI18nTextBundle'
                      ]
                    }
                  ]
                },
                {
                  declare: [
                    'text',
                    {
                      find: [
                        { var: 'textBundle' },
                        {
                          '===': [
                            { var: 'it.key'},
                            { var: 'key' }
                          ]
                        },
                        'it'
                      ]
                    }
                  ]
                },
                {
                  if: [
                    {
                      missing: [ 'text' ]
                    },
                    {
                      return: [
                        null
                      ]
                    }
                  ]
                },
                {
                  declare: [
                    'textInLang',
                    {
                      find: [
                        { var: 'text.texts' },
                        {
                          '===': [
                            { var: 'it.lang'},
                            { var: 'lang'}
                          ]
                        },
                        'it'
                      ]
                    }
                  ]
                },
                {
                  if: [
                    { var: 'textInLang' },
                    {
                      return: [
                        { var: 'textInLang.text'} 
                      ]
                    }
                  ]
                },
                {
                  declare: [
                    'textInDefaultLang',
                    {
                      find: [
                        { var: 'text.texts' },
                        {
                          '===': [
                            { var: 'it.lang'},
                            { var: 'defaultLang'}
                          ]
                        },
                        'it'
                      ]
                    }
                  ]
                },
                {
                  if: [
                    { var: 'textInDefaultLang' },
                    {
                      return: [
                        { var: 'textInDefaultLang.text'} 
                      ]
                    },
                    {
                      return: [
                        null
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
        val getI18nTextByKeyNode = JsonNodeFactory.instance.rawValueNode(RawValue(getI18nTextByKeyJson))
        registerFunction("getI18nTextByKey", getI18nTextByKeyNode)

        val greetMultilingualJson = """
            {
              parameters: [
                { name: 'first' },
                { name: 'last' }
              ],
              logic: [
                {
                  declare: [
                    'name',
                    {
                      concatenate: [
                        { var: 'first' },
                        { var: 'last' }
                      ]
                    }
                  ]
                },
                {
                  return: [
                    {
                      init: [
                        'object',
                        'greetingDE', {
                          call: [
                            'i18n',
                            'GREETING',
                            'de',
                            'en',
                            [ { var: 'name' } ]
                          ]
                        },
                        'greetingES', {
                          call: [
                            'i18n',
                            'GREETING',
                            'es',
                            'en',
                            [ { var: 'name' } ]
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
        val greetMultilingualNode = JsonNodeFactory.instance.rawValueNode(RawValue(greetMultilingualJson))
        registerFunction("greetMultilingual", greetMultilingualNode)

        val rawInput = "{ first: 'John', last: 'Doe' }"
        val inputNode = JsonNodeFactory.instance.rawValueNode(RawValue(rawInput))
        val output = evaluateFunction("greetMultilingual", inputNode)

        assertEquals(
            """
                {
                    greetingDE: 'Hallo John Doe',
                    greetingES: '¡John Doe, hola John Doe!'
                }
            """.trimIndent(),
            output.toString()
        )
    }
}
