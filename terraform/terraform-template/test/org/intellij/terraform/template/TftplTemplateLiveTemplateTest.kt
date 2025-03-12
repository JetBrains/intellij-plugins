package org.intellij.terraform.template

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.codeInsight.template.impl.actions.ListTemplatesAction
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TftplTemplateLiveTemplateTest : BasePlatformTestCase() {
  fun `test for loop`() {
    doTest("for<caret>",
           """
             %{~ for  in  ~}
               
             %{~ endfor ~}
           """.trimIndent())
  }

  fun `test if condition`() {
    doTest("if<caret>",
           """
             %{~ if  ~}
               
             %{~ endif ~}
           """.trimIndent())
  }

  private fun doTest(editorText: String, expected: String) {
    myFixture.configureByText("test.tftpl", editorText)

    val editor = myFixture.editor

    ListTemplatesAction().actionPerformedImpl(project, editor)
    val lookup = LookupManager.getActiveLookup(editor) as? LookupImpl
    lookup?.finishLookup(Lookup.NORMAL_SELECT_CHAR)
    TemplateManagerImpl.getTemplateState(editor)?.let { Disposer.dispose(it) }

    myFixture.checkResult(expected)
  }
}