package org.jetbrains.astro.codeInsight

import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroHighlightingTest : AstroCodeInsightTestCase() {

  fun testCharEntityResolution() = doTest()

  fun testUnusedComponentImports() = doTest(additionalFiles = listOf("component.astro"))

  //region Test configuration and helper methods

  override fun getBasePath(): String = "codeInsight/highlighting"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(AstroInspectionsProvider())
  }

  private fun doTest(additionalFiles: List<String> = emptyList()) {
    configure(additionalFiles = additionalFiles)
    myFixture.checkHighlighting()
  }

  //endregion
}