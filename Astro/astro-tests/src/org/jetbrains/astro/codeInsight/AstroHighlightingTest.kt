package org.jetbrains.astro.codeInsight

import com.intellij.psi.PsiFile
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroHighlightingTest: AstroCodeInsightTestCase() {

  fun testCharEntityResolution() = doTest()

  //region Test configuration and helper methods

  override fun getBasePath(): String = "codeInsight/highlighting"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(AstroInspectionsProvider())
  }

  private fun doTest(extension: String = "astro",
                     vararg additionalFiles: String) {
    configureTestProject(extension, *additionalFiles)
    myFixture.checkHighlighting()
  }

  private fun configureTestProject(extension: String = "astro",
                                   vararg additionalFiles: String): PsiFile {
    myFixture.configureByFiles(*additionalFiles)
    return myFixture.configureByFile(getTestName(true) + "." + extension)
  }
  //endregion
}