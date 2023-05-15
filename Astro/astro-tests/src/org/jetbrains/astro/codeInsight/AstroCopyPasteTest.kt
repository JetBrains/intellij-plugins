package org.jetbrains.astro.codeInsight

import com.intellij.openapi.actionSystem.IdeActions
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroCopyPasteTest: AstroCodeInsightTestCase() {

  fun testBasic() {
    doTest()
  }

  fun testFrontmatterToJsx() {
    doTest()
  }

  //region Test configuration and helper methods
  override fun getBasePath(): String {
    return "codeInsight/copyPaste"
  }
  private fun doTest() {
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("Source.astro")
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COPY)
    myFixture.configureFromTempProjectFile("Destination.astro")
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE)
    myFixture.checkResultByFile(getTestName(true) + "/Destination_after.astro")
  }
  //endregion

}