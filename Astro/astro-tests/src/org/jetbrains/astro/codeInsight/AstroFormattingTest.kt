package org.jetbrains.astro.codeInsight

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroFormattingTest : AstroCodeInsightTestCase("codeInsight/formatting") {

  fun testBasic() = doTest()

  fun testWhitespacesBeforeFrontmatter() = doTest()

  fun testScriptTag() = doTest()

  //region Test configuration and helper methods
  private fun doTest() {
    doFormattingTest()
  }
  //endregion
}