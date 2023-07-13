package org.jetbrains.astro.codeInsight

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
    configure(dir = true, configureFile = false)
    performCopyPaste("Source.astro", "Destination.astro")
    myFixture.checkResultByFile("$testName/Destination_after.astro")
  }
  //endregion

}