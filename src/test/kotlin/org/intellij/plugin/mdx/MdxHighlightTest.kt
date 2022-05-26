package org.intellij.plugin.mdx

import com.intellij.lang.javascript.inspections.JSUnresolvedReactComponentInspection
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection
import com.intellij.lang.javascript.modules.TypeScriptCheckImportInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedFunctionInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedVariableInspection
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection
import org.junit.Test

class MdxHighlightTest : MdxTestBase() {
    private fun doTestHighlighting(text: String) {
        myFixture.enableInspections(
            TypeScriptCheckImportInspection(),
            TypeScriptValidateTypesInspection(),
            JSUnresolvedReactComponentInspection(),
            TypeScriptUnresolvedVariableInspection(),
            TypeScriptUnresolvedFunctionInspection(),
            JSUnresolvedVariableInspection()
        )

        myFixture.configureByText(
            "foo.mdx",
            text
        )

        myFixture.testHighlighting()
    }


    @Test
    fun testJsxSimple() {
        doTestHighlighting("<<weak_warning>Button</weak_warning>>Press the button</Button>")
    }

    @Test
    fun testUnresolvedVariable() {
        val text = """import {h} from 'hello.mdx'
        
        <div>{<weak_warning descr="Unresolved variable or type hello">hello</weak_warning>}</div>
        """.trimMargin()
        doTestHighlighting(text)
    }

    @Test
    fun testParsingError() {
        val text = """
            * <<weak_warning>DocLink1</weak_warning> />
            * <<weak_warning>DocLink2</weak_warning> />
        """.trimIndent()
        
        doTestHighlighting(text)
    }

    @Test
    fun testParsingErrorOneLine() {
        val text = """
            * <<weak_warning>DocLink</weak_warning> />
        """.trimIndent()

        doTestHighlighting(text)
    }

    @Test
    fun testComment() {
        doTestHighlighting(
            """
                <!--
                <Unknown>{`
                  .subheading {sad
                    --mediumdark: '#999999';
                    font-we1ight: 900;
                    font-size: 13px;ads
                  }
                `}</Unknown>
                -->
            """.trimIndent()
        )
    }

    @Test
    fun testOpenTagInAttribute() {
        doTestHighlighting(
            """
                
                <<weak_warning>CodeTabs</weak_warning>
                    tregx={``}
                    php={`<`}/>

                Of course, `first()` callback will asd qwe

                :::note
            """.trimIndent()
        )
    }

    @Test
    fun testOpenCloseTagInAttribute() {
        doTestHighlighting(
            """
               
                <<weak_warning>CodeTabs</weak_warning>
                    tregx={`<>`}
                    />

                Of course, `first()` callback will asd qwe

                :::note
            """.trimIndent()
        )
    }

    @Test
    fun testOpenCloseTagIn2Attribute() {
        doTestHighlighting(
            """
                
                <<weak_warning>CodeTabs</weak_warning>
                    tregx={`<>`} tregx={`<>`}
                    />

                Of course, `first()` callback will asd qwe

                :::note
            """.trimIndent()
        )
    }

    @Test
    fun testOpenCloseTagIn2AttributeSameLine() {
        doTestHighlighting(
            """
                
                <<weak_warning>CodeTabs</weak_warning>
                    tregx={`<>`} tregx={`<>`}/>

                Of course, `first()` callback will asd qwe

                :::note
            """.trimIndent()
        )
    }
}