package com.intellij.terraform.template

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.codeInsight.template.impl.actions.ListTemplatesAction
import com.intellij.openapi.application.EDT
import com.intellij.openapi.util.Disposer
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test

@TestApplication
class TftplTemplateLiveTemplateTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  @Test
  fun `test for loop`() {
    doTest("for<caret>",
           """
             %{~ for  in  ~}
               
             %{~ endfor ~}
           """.trimIndent())
  }

  @Test
  fun `test if condition`() {
    doTest("if<caret>",
           """
             %{~ if  ~}
               
             %{~ endif ~}
           """.trimIndent())
  }

  private fun doTest(editorText: String, expected: String): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      myFixture.configureByText("test.tftpl", editorText)

      val editor = myFixture.editor

      ListTemplatesAction().actionPerformedImpl(myFixture.project, editor)
      val lookup = LookupManager.getActiveLookup(editor) as? LookupImpl
      lookup?.finishLookup(Lookup.NORMAL_SELECT_CHAR)
      TemplateManagerImpl.getTemplateState(editor)?.let { Disposer.dispose(it) }

      myFixture.checkResult(expected)
    }
  }
}