package org.jetbrains.astro.codeInsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.webSymbols.checkDocumentationAtCaret
import com.intellij.webSymbols.checkLookupItems
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroDocumentationTest : AstroCodeInsightTestCase("codeInsight/documentation") {

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
  private fun doTest() {
    configure()
    myFixture.checkDocumentationAtCaret()
  }

  private fun doLookupTest(
    lookupFilter: (item: LookupElement) -> Boolean = { true }
  ) {
    configure()
    myFixture.checkLookupItems(renderPriority = true, renderTypeText = true, checkDocumentation = true, lookupItemFilter = lookupFilter)
  }
  //endregion

}