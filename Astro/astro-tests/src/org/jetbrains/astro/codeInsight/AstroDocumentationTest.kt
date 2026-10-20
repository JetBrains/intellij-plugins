package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.typescript.documentation.TypeScriptDocumentationTargetProvider
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.polySymbols.testFramework.LookupElementInfo
import com.intellij.polySymbols.testFramework.checkDocumentationAtCaret
import com.intellij.polySymbols.testFramework.checkLookupItems
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class AstroDocumentationTest : AstroCodeInsightTestCase("codeInsight/documentation", useLsp = true) {

  @BeforeEach
  fun registerTypeScriptDocumentation() {
    PsiDocumentationTargetProvider.EP_NAME.point
      .registerExtension(TypeScriptDocumentationTargetProvider(), astro.testRootDisposable)
    JSLanguageServiceUtil.setTimeout(10000, myFixture.testRootDisposable)
  }

  @Test
  fun testHtmlTag() = doTest()

  @Test
  fun testHtmlAttribute() = doTest()

  @Test
  fun testHtmlTagLookup() = doLookupTest {
    it.lookupString in listOf("div", "table")
  }

  @Test
  fun testHtmlAttributeLookup() = doLookupTest {
    it.lookupString in listOf("src", "title")
  }

  @Test
  fun testAstroDirective() = doTest()

  @Test
  fun testComponentProp() {
    astro.doConfiguredTest(additionalFiles = listOf("component.astro")) {
      checkDocumentationAtCaret()
    }
  }

  //region Test configuration and helper methods
  private fun doTest() {
    astro.doConfiguredTest {
      checkDocumentationAtCaret()
    }
  }

  private fun doLookupTest(
    lookupFilter: (item: LookupElementInfo) -> Boolean = { true },
  ) {
    astro.doConfiguredTest {
      checkLookupItems(renderPriority = true, renderTypeText = true, checkDocumentation = true, lookupItemFilter = lookupFilter)
    }
  }
  //endregion

}