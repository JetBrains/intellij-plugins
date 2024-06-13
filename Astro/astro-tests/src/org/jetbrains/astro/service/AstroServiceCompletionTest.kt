package org.jetbrains.astro.service

import com.intellij.platform.lsp.tests.checkLspHighlighting
import org.jetbrains.astro.checkCompletionContains
import org.jetbrains.astro.getRelativeAstroTestDataPath


class AstroServiceCompletionTest : AstroServiceTestBase() {
  override fun getBasePath(): String = getRelativeAstroTestDataPath() + "/service/completion/"

  fun testAwait() {
    defaultCompletionTest()
    val lookupElements = myFixture.completeBasic()
    lookupElements.checkCompletionContains("await")
  }

  private fun defaultCompletionTest() {
    configureDefault()
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }
}