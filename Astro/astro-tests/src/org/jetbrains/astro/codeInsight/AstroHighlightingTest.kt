package org.jetbrains.astro.codeInsight

import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroHighlightingTest : AstroCodeInsightTestCase("codeInsight/highlighting") {

  fun testCharEntityResolution() = doTest()

  fun testUnusedComponentImports() = doTest(additionalFiles = listOf("component.astro"))

  fun testClientDirectives() = doTest(additionalFiles = listOf("component.astro"))

  fun testUnusedImportFalsePositive() = doTest()

  //region Test configuration and helper methods

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