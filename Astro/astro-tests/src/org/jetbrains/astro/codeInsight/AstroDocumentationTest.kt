package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.typescript.documentation.TypeScriptDocumentationTargetProvider
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.polySymbols.testFramework.LookupElementInfo
import com.intellij.polySymbols.testFramework.checkDocumentationAtCaret
import com.intellij.polySymbols.testFramework.checkLookupItems
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroDocumentationTest : AstroCodeInsightTestCase("codeInsight/documentation", useLsp = true) {

  override fun setUp() {
    super.setUp()
    PsiDocumentationTargetProvider.EP_NAME.point
      .registerExtension(TypeScriptDocumentationTargetProvider(), testRootDisposable)
    JSLanguageServiceUtil.setTimeout(10000, myFixture.testRootDisposable)
  }

  fun testHtmlTag() = doTest()

  fun testHtmlAttribute() = doTest()

  fun testHtmlTagLookup() = doLookupTest {
    it.lookupString in listOf("div", "table")
  }

  fun testHtmlAttributeLookup() = doLookupTest {
    it.lookupString in listOf("src", "title")
  }

  fun testAstroDirective() = doTest()

  fun testComponentProp() {
    doConfiguredTest(additionalFiles = listOf("component.astro")) {
      checkDocumentationAtCaret()
    }
  }

  //region Test configuration and helper methods
  private fun doTest() {
    doConfiguredTest {
      checkDocumentationAtCaret()
    }
  }

  private fun doLookupTest(
    lookupFilter: (item: LookupElementInfo) -> Boolean = { true },
  ) {
    doConfiguredTest {
      checkLookupItems(renderPriority = true, renderTypeText = true, checkDocumentation = true, lookupItemFilter = lookupFilter)
    }
  }
  //endregion

}