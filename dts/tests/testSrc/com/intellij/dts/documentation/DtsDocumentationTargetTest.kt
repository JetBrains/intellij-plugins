package com.intellij.dts.documentation

import com.intellij.dts.DtsTestBase
import com.intellij.lang.documentation.ide.IdeDocumentationTargetProvider
import com.intellij.openapi.application.readAction
import com.intellij.openapi.progress.runBlockingCancellable

class DtsDocumentationTargetTest : DtsTestBase() {
  override fun runInDispatchThread(): Boolean = false

  override fun runFromCoroutine(): Boolean = true

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

  private fun assertOneDocumentationTarget() {
    val provider = IdeDocumentationTargetProvider.getInstance(project)

    val targets = runBlockingCancellable {
      readAction {
        provider.documentationTargets(myFixture.editor, myFixture.file, myFixture.caretOffset)
      }
    }

    assertOneElement(targets)
  }

  private fun doTest(text: String) {
    configureByText(text)

    assertOneDocumentationTarget()
  }

  private fun doLookupTest(text: String) {
    configureByText(text)

    val completion = myFixture.completeBasic().toList()
    assertNotEmpty(completion)

    assertOneDocumentationTarget()
  }
}