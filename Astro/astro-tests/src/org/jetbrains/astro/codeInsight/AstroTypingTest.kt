package org.jetbrains.astro.codeInsight

import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroTypingTest : AstroCodeInsightTestCase() {

  fun testBracesInExpressions() = doTest("{([{")

  //region Test configuration and helper methods
  override fun getBasePath(): String {
    return "codeInsight/typing"
  }

  private fun doTest(textToType: String) {
    configure()
    myFixture.type(textToType)
    checkResult()
  }
  //endregion
}