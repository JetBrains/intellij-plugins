package org.jetbrains.astro.codeInsight.highlighting

import com.intellij.polySymbols.testFramework.HybridFixtureContext
import com.intellij.testFramework.junit5.fixture.TestFixture
import org.jetbrains.astro.AstroFixture
import org.jetbrains.astro.AstroTestMode
import org.jetbrains.astro.astroFixture
import org.jetbrains.astro.codeInsight.AstroInspectionsProvider

/**
 * Creates an [AstroHighlightingFixture] for the test data under [testCasePath]. The tests share the light project.
 * Declare the result as an instance property.
 */
fun astroHighlightingFixture(testCasePath: String): TestFixture<AstroHighlightingFixture> =
  astroFixture(AstroTestMode.NO_SERVICE) { AstroHighlightingFixture(it, testCasePath, AstroTestMode.NO_SERVICE) }

/**
 * An [AstroFixture] that enables the [AstroInspectionsProvider] inspections.
 */
class AstroHighlightingFixture(context: HybridFixtureContext, testCasePath: String, testMode: AstroTestMode) : AstroFixture(context, testCasePath, testMode) {
  override fun setUp() {
    super.setUp()
    fixture.enableInspections(AstroInspectionsProvider())
  }

  /**
   * Configures the test file and [additionalFiles], and checks the highlighting.
   */
  fun doTest(additionalFiles: List<String> = emptyList()) {
    doConfiguredTest(additionalFiles = additionalFiles) {
      checkHighlighting()
    }
  }
}
