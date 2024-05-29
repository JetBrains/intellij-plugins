package org.jetbrains.astro.codeInsight.highlighting

import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.codeInsight.AstroInspectionsProvider

abstract class AstroHighlightingTestBase(testCasePath: String) : AstroCodeInsightTestCase(testCasePath) {
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(AstroInspectionsProvider())
  }

  protected fun doTest(additionalFiles: List<String> = emptyList()) {
    doConfiguredTest(additionalFiles = additionalFiles) {
      checkHighlighting()
    }
  }
}