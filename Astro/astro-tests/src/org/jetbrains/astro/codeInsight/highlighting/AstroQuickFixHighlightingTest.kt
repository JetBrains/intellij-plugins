package org.jetbrains.astro.codeInsight.highlighting

import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroFixture
import org.jetbrains.astro.AstroTestMode
import org.jetbrains.astro.astroFixture
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class AstroQuickFixHighlightingTest :
  AstroQuickFixHighlightingTestBase() {

  @TestApplication
  class WithTsGoProxyTest :
    AstroQuickFixHighlightingTestBase(testMode = AstroTestMode.TS_GO_PROXY)
}

abstract class AstroQuickFixHighlightingTestBase(
  testMode: AstroTestMode = AstroTestMode.ASTRO_LSP,
) {

  private val astro: AstroFixture by astroFixture("codeInsight/highlighting/quickFix", testMode)

  @BeforeEach
  fun allowDocumentChangesDuringHighlighting() {
    (astro.fixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
  }

  @Test
  fun testAutoImport() {
    astro.doConfiguredTest(additionalFiles = listOf("MyComponent.astro"), checkResult = true) {
      checkHighlighting()
      val intention = availableIntentions.firstOrNull { it.text.contains("import") && it.text.contains("MyComponent") }
                      ?: availableIntentions.firstOrNull { it.familyName.contains("import", ignoreCase = true) && it.text.contains("MyComponent") }

      assertNotNull(intention, "Auto-import quick fix for 'MyComponent' was not found among available intentions")
      launchAction(intention!!)
    }
  }
}