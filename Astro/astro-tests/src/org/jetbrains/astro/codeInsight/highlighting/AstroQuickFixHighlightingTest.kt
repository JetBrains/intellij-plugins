package org.jetbrains.astro.codeInsight.highlighting

import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.AstroTestMode

class AstroQuickFixHighlightingTest : AstroQuickFixHighlightingTestBase() {

  class WithTsGoProxyTest : AstroQuickFixHighlightingTestBase(testMode = AstroTestMode.TS_GO_PROXY)
}

abstract class AstroQuickFixHighlightingTestBase(
  testMode: AstroTestMode = AstroTestMode.ASTRO_LSP,
) : AstroCodeInsightTestCase("codeInsight/highlighting/quickFix", testMode) {

  override fun setUp() {
    super.setUp()
    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
  }

  fun testAutoImport() {
    doConfiguredTest(additionalFiles = listOf("MyComponent.astro"), checkResult = true) {
      checkHighlighting()
      val intention = availableIntentions.firstOrNull { it.text.contains("import") && it.text.contains("MyComponent") }
                      ?: availableIntentions.firstOrNull { it.familyName.contains("import", ignoreCase = true) && it.text.contains("MyComponent") }

      assertNotNull("Auto-import quick fix for 'MyComponent' was not found among available intentions", intention)
      launchAction(intention!!)
    }
  }
}