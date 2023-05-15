package org.jetbrains.astro.codeInsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.webSymbols.checkListByFile
import com.intellij.webSymbols.moveToOffsetBySignature
import com.intellij.webSymbols.renderLookupItems
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.AstroTestModule

class AstroCompletionTest : AstroCodeInsightTestCase() {

  fun testHtmlElements() = doLookupTest()
  fun testHtmlAttributes() = doLookupTest()
  fun testCharEntities() = doLookupTest()

  fun testScriptTagAttributes() =
    doLookupTest { !it.endsWith("#0") }

  fun testStyleTagAttributes() =
    doLookupTest { !it.endsWith("#0") }

  fun testImportedComponent() =
    doLookupTest(additionalFiles = listOf("component.astro"))

  fun testImportComponent() =
    doTypingTest("Compo\n", additionalFiles = listOf("component.astro"))

  fun testImportExternalSymbolFrontmatter() =
    doTypingTest("olo\n", additionalFiles = listOf("colors.ts"))

  fun testImportExternalSymbolExpression() =
    doTypingTest("olo\n", additionalFiles = listOf("colors.ts"))

  fun testFrontmatterKeywords() =
    doLookupTest(additionalFiles = listOf("component.astro"),
                 filter = { it.endsWith("#6") || it.endsWith("#5") })

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
  override fun getBasePath(): String {
    return "codeInsight/completion"
  }

  private fun doTypingTest(textToType: String,
                           additionalFiles: List<String> = emptyList(),
                           vararg modules: AstroTestModule) {
    configure(additionalFiles = additionalFiles, modules = modules)
    myFixture.completeBasic()
    myFixture.type(textToType)
    checkResult()
  }

  private fun doLookupTest(vararg modules: AstroTestModule,
                           fileContents: String? = null,
                           dir: Boolean = false,
                           noConfigure: Boolean = false,
                           locations: List<String> = emptyList(),
                           renderPriority: Boolean = true,
                           renderTypeText: Boolean = true,
                           renderTailText: Boolean = false,
                           containsCheck: Boolean = false,
                           renderProximity: Boolean = false,
                           renderPresentedText: Boolean = false,
                           additionalFiles: List<String> = emptyList(),
                           lookupFilter: (item: LookupElement) -> Boolean = { true },
                           filter: (item: String) -> Boolean = { true }) {
    if (!noConfigure) {
      configure(fileContents, dir, additionalFiles, *modules)
    }
    if (locations.isEmpty()) {
      myFixture.completeBasic()
      myFixture.checkListByFile(
        myFixture.renderLookupItems(renderPriority, renderTypeText, renderTailText, renderProximity, renderPresentedText, lookupFilter)
          .filter(filter),
        getTestName(true) + ".txt",
        containsCheck
      )
    }
    else {
      locations.forEachIndexed { index, location ->
        myFixture.moveToOffsetBySignature(location)
        myFixture.completeBasic()
        myFixture.checkListByFile(
          myFixture.renderLookupItems(renderPriority, renderTypeText, renderTailText, renderProximity, renderPresentedText, lookupFilter)
            .filter(filter),
          getTestName(true) + ".${index + 1}.txt",
          containsCheck
        )
      }
    }
  }
  //endregion

}