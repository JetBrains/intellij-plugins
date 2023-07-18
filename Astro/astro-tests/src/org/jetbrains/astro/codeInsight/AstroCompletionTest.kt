package org.jetbrains.astro.codeInsight

import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.AstroTestModule

class AstroCompletionTest : AstroCodeInsightTestCase("codeInsight/completion") {

  fun testHtmlElements() = doLookupTest()
  fun testHtmlAttributes() = doLookupTest()
  fun testCharEntities() = doLookupTest()

  fun testScriptTagAttributes() =
    doLookupTest(renderedItemFilter = { !it.endsWith("#0") })

  fun testStyleTagAttributes() =
    doLookupTest(renderedItemFilter = { !it.endsWith("#0") })

  fun testImportedComponent() =
    doLookupTest(additionalFiles = listOf("component.astro"))

  fun testImportComponent() =
    doTypingTest("Compo\n", additionalFiles = listOf("component.astro"))

  fun testImportExternalSymbolFrontmatter() =
    doTypingTest("olo\n", additionalFiles = listOf("colors.ts"))

  fun testImportExternalSymbolExpression() =
    doTypingTest("olo\n", additionalFiles = listOf("colors.ts"))

  fun testImportWithinScriptBlock() =
    doTypingTest("getRandomNumber\n", additionalFiles = listOf("functions.ts"))

  fun testFrontmatterKeywords() =
    doLookupTest(additionalFiles = listOf("component.astro"),
                 renderedItemFilter = { it.endsWith("#6") || it.endsWith("#5") })

  fun testPropsInterface() =
    doLookupTest(AstroTestModule.ASTRO_1_9_0)

  fun testTemplateLookupRoot() =
    doLookupTest()

  fun testTemplateLookupNestedHtml() =
    doLookupTest()

  // WEB-59265 only enabled completion at root level and nested in HTML but not as children of components.
  // This needs a fix before it can be enabled again.
  //fun testTemplateLookupNestedComponent() =
  //  doLookupTest(additionalFiles = listOf("component.astro"))

  //region Test configuration and helper methods

  private fun doTypingTest(textToType: String,
                           additionalFiles: List<String> = emptyList(),
                           vararg modules: AstroTestModule) {
    doConfiguredTest(additionalFiles = additionalFiles, modules = modules) {
      completeBasic()
      type(textToType)
      checkResult()
    }
  }

  //endregion

}