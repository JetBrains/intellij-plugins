package org.jetbrains.astro.codeInsight

import com.intellij.webSymbols.performCopyPaste
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroCopyPasteTest : AstroCodeInsightTestCase("codeInsight/copyPaste") {

  fun testBasic() {
    doTest()
  }

  fun testFrontmatterToJsx() {
    doTest()
  }

  //region Test configuration and helper methods
  private fun doTest() {
    doConfiguredTest(dir = true, configureFile = false) {
      performCopyPaste("Source.astro", "Destination.astro")
      checkResultByFile("$testName/Destination_after.astro")
    }
  }
  //endregion

}