package org.jetbrains.astro.codeInsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.webSymbols.checkDocumentationAtCaret
import com.intellij.webSymbols.checkLookupElementDocumentationAtCaret
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroDocumentationTest : AstroCodeInsightTestCase() {

  fun testHtmlTag() = doTest()

  fun testHtmlAttribute() = doTest()

  fun testHtmlTagLookup() = doLookupTest {
    it.lookupString in listOf("div", "table")
  }

  fun testHtmlAttributeLookup() = doLookupTest {
    it.lookupString in listOf("src", "title")
  }

  fun testAstroDirective() = doTest()

  //region Test configuration and helper methods
  override fun getBasePath(): String {
    return "codeInsight/documentation"
  }

  private fun doTest() {
    configure()
    myFixture.checkDocumentationAtCaret()
  }

  private fun doLookupTest(
    lookupFilter: (item: LookupElement) -> Boolean = { true }
  ) {
    configure()
    myFixture.checkLookupElementDocumentationAtCaret(renderPriority = true, renderTypeText = true, lookupFilter = lookupFilter)
  }
  //endregion

}