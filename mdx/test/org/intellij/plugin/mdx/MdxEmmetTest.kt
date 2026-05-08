package org.intellij.plugin.mdx

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.openapi.command.WriteCommandAction
import org.junit.Test

class MdxEmmetTest : MdxTestBase() {
    
    @Test
    fun testTemplates() {
        doTest("a:link<caret>", "<a href=\"http://\"></a>")
    }

    @Test
    fun testTagNameInference() {
        doTest("ul>.item*3<caret>", """<ul>
    <li></li>
    <li></li>
    <li></li>
</ul>""")
        doTest("<ul>.item*3<caret></ul>", """<ul>
    <li className="item"></li>
    <li className="item"></li>
    <li className="item"></li></ul>""")
    }

    @Test
    fun testDoubleBracket() {
        doTest("<inp<caret>", "<inp")
    }

    private fun doTest(input: String, expectedOutput: String) {
        myFixture.configureByText("a.mdx", input)
        TemplateManagerImpl.setTemplateTesting(testRootDisposable)
        WriteCommandAction.runWriteCommandAction(myFixture.project) { TemplateManager.getInstance(myFixture.project)
            .startTemplate(myFixture.editor, TemplateSettings.TAB_CHAR) }
        myFixture.checkResult(expectedOutput)
    }
}