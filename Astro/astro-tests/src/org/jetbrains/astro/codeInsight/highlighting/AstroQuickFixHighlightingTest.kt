package org.jetbrains.astro.codeInsight.highlighting

import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class AstroQuickFixHighlightingTest : AstroCodeInsightTestCase("codeInsight/highlighting/quickFix", useLsp = true) {

  @BeforeEach
  fun allowDocumentChangesDuringHighlighting() {
    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
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