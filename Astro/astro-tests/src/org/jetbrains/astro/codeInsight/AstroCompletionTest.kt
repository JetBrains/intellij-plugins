package org.jetbrains.astro.codeInsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.webSymbols.checkListByFile
import com.intellij.webSymbols.moveToOffsetBySignature
import com.intellij.webSymbols.renderLookupItems
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroCompletionTest : AstroCodeInsightTestCase() {

  fun testHtmlElements() = doLookupTest()
  fun testHtmlAttributes() = doLookupTest()
  fun testCharEntities() = doLookupTest()

  fun testImportedComponent() =
    doLookupTest(additionalFiles = listOf("component.astro"))

  fun testImportExternalSymbolFrontmatter() =
    doTypingTest("olo\n", additionalFiles = listOf("colors.ts"))

  fun testImportExternalSymbolExpression() =
    doTypingTest("olo\n", additionalFiles = listOf("colors.ts"))

  //region Test configuration and helper methods
  override fun getBasePath(): String {
    return "codeInsight/completion"
  }

  private fun doTypingTest(textToType: String,
                           additionalFiles: List<String>) {
    if (additionalFiles.isNotEmpty()) {
      myFixture.configureByFiles(*additionalFiles.toTypedArray())
    }
    myFixture.configureByFiles(getTestName(true) + ".astro")
    myFixture.completeBasic()
    myFixture.type(textToType)
    myFixture.checkResultByFile(getTestName(true) + "_after.astro")
  }

  private fun doLookupTest(fileContents: String? = null,
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
      if (dir) {
        myFixture.copyDirectoryToProject(getTestName(true), ".")
      }
      else if (additionalFiles.isNotEmpty()) {
        myFixture.configureByFiles(*additionalFiles.toTypedArray())
      }
      if (fileContents != null) {
        myFixture.configureByText(getTestName(true) + ".astro", fileContents)
      }
      else if (dir) {
        myFixture.configureFromTempProjectFile(getTestName(true) + ".astro")
      }
      else {
        myFixture.configureByFile(getTestName(true) + ".astro")
      }
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