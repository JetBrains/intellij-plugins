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

  fun `test lookup label ref`() = doLookupTest("""
      prop = &<caret>;
      label1: node {}; 
    """)

  fun `test lookup path ref`() = doLookupTest("""
      / { node1 {}; node2 {}; };
      &{/<caret>} {};
    """)

  private fun doTest(text: String) {
    configureByText(text)

    val targets = IdeDocumentationTargetProvider
      .getInstance(project)
      .documentationTargets(myFixture.editor, myFixture.file, myFixture.caretOffset)

    assertOneElement(targets)
  }

  private fun doLookupTest(text: String) {
    configureByText(text)

    val completion = myFixture.completeBasic().toList()
    assertNotEmpty(completion)

    val targets = IdeDocumentationTargetProvider
      .getInstance(project)
      .documentationTargets(myFixture.editor, myFixture.file, completion.first())

    assertOneElement(targets)
  }
}