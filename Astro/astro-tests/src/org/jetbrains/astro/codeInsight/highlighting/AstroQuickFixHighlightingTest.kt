package org.jetbrains.astro.codeInsight.highlighting

import com.intellij.platform.lsp.tests.checkLspHighlighting
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroQuickFixHighlightingTest : AstroCodeInsightTestCase("codeInsight/highlighting/quickFix", useLsp = true) {

  override fun setUp() {
    super.setUp()
    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
  }

  fun testAutoImport() {
    doConfiguredTest(additionalFiles = listOf("MyComponent.astro"), checkResult = true) {
      checkLspHighlighting()
      val intention = availableIntentions.firstOrNull { it.text.contains("import") && it.text.contains("MyComponent") }
                      ?: availableIntentions.firstOrNull { it.familyName.contains("import", ignoreCase = true) && it.text.contains("MyComponent") }

      assertNotNull("Auto-import quick fix for 'MyComponent' was not found among available intentions", intention)
      launchAction(intention!!)
    }
  }
}