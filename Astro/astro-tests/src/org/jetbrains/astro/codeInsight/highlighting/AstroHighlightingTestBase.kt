package org.jetbrains.astro.codeInsight.highlighting

import com.intellij.polySymbols.testFramework.HybridFixtureContext
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.AstroFixture
import org.jetbrains.astro.codeInsight.AstroInspectionsProvider

abstract class AstroHighlightingTestBase(testCasePath: String) : AstroCodeInsightTestCase(testCasePath, false, ::AstroHighlightingFixture) {
  protected fun doTest(additionalFiles: List<String> = emptyList()) {
    astro.doConfiguredTest(additionalFiles = additionalFiles) {
      checkHighlighting()
    }
  }
}

/**
 * The state of [AstroHighlightingTestBase]: an [AstroFixture] that enables the [AstroInspectionsProvider] inspections.
 */
class AstroHighlightingFixture(context: HybridFixtureContext, testCasePath: String, useLsp: Boolean) : AstroFixture(context, testCasePath, useLsp) {
  override fun setUp() {
    super.setUp()
    fixture.enableInspections(AstroInspectionsProvider())
  }
}
