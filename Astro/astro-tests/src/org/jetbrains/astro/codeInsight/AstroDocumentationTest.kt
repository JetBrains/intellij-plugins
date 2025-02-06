package org.jetbrains.astro.codeInsight

import com.intellij.webSymbols.testFramework.LookupElementInfo
import com.intellij.webSymbols.testFramework.checkDocumentationAtCaret
import com.intellij.webSymbols.testFramework.checkLookupItems
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
    doConfiguredTest {
      checkDocumentationAtCaret()
    }
  }

  private fun doLookupTest(
    lookupFilter: (item: LookupElementInfo) -> Boolean = { true }
  ) {
    doConfiguredTest {
      checkLookupItems(renderPriority = true, renderTypeText = true, checkDocumentation = true, lookupItemFilter = lookupFilter)
    }
  }
  //endregion

}