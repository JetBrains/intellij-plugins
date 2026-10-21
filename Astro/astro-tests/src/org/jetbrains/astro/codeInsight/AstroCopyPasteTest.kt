package org.jetbrains.astro.codeInsight

import com.intellij.javascript.testFramework.web.configureAndCopyPaste
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroFixture
import org.jetbrains.astro.astroFixture
import org.junit.jupiter.api.Test

@TestApplication
class AstroCopyPasteTest {

  private val astro: AstroFixture by astroFixture("codeInsight/copyPaste")

  @Test
  fun testBasic() {
    doTest()
  }

  @Test
  fun testFrontmatterToJsx() {
    doTest()
  }

  //region Test configuration and helper methods
  private fun doTest() {
    astro.doConfiguredTest(dir = true, configureFile = false) {
      configureAndCopyPaste("Source.astro", "Destination.astro")
      checkResultByFile("${astro.testName}/Destination_after.astro")
    }
  }
  //endregion

}