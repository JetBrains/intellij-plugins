package com.intellij.dts.documentation

import com.intellij.dts.DtsTestBase
import com.intellij.lang.documentation.ide.IdeDocumentationTargetProvider

class DtsDocumentationTargetTest : DtsTestBase() {
    fun `test start of node name`() = doTest("""
       <caret>node {}; 
    """)

    fun `test end of node name`() = doTest("""
       node<caret> {}; 
    """)

    fun `test start of property name`() = doTest("""
       <caret>prop = "value"; 
    """)

    fun `test end of property name`() = doTest("""
       prop<caret> = "value"; 
    """)

    fun `test start of ref`() = doTest("""
       prop = <caret>&label; 
       label: node {};
    """)

    fun `test end of ref`() = doTest("""
       prop = &label<caret>; 
       label: node {};
    """)

    private fun doTest(text: String) {
        configureByText(text)

        val targets = IdeDocumentationTargetProvider
            .getInstance(project)
            .documentationTargets(myFixture.editor, myFixture.file, myFixture.caretOffset)

        assertOneElement(targets)
    }
}