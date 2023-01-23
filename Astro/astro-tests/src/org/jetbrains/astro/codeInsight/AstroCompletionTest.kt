package org.jetbrains.astro.codeInsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.webSymbols.checkListByFile
import com.intellij.webSymbols.moveToOffsetBySignature
import com.intellij.webSymbols.renderLookupItems
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroCompletionTest: AstroCodeInsightTestCase() {

  fun testHtmlElements() = doLookupTest()
  fun testHtmlAttributes() = doLookupTest()
  fun testCharEntities() = doLookupTest()

  fun testImportedComponent() {
    myFixture.configureByFile("component.astro")
    doLookupTest()
  }

  //region Test configuration and helper methods
  override fun getBasePath(): String {
    return "codeInsight/completion"
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
    lookupFilter: (item: LookupElement) -> Boolean = { true },
    filter: (item: String) -> Boolean = { true }) {
    if (!noConfigure) {
      if (dir) {
        myFixture.copyDirectoryToProject(getTestName(true), ".")
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