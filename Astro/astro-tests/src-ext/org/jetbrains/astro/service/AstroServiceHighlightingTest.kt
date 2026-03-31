package org.jetbrains.astro.service

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.platform.lsp.tests.checkLspHighlighting
import com.intellij.platform.lsp.tests.waitForDiagnosticsFromLspServer
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroServiceHighlightingTest : AstroCodeInsightTestCase("codeInsight/highlighting/service", useLsp = true) {

  override fun setUp() {
    super.setUp()
    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
  }

  fun testHighlightingUpdatesAfterRemovingDeclaration() {
    doConfiguredTest {
      checkLspHighlighting()

      val document = getDocument(file)

      // remove declaration
      WriteCommandAction.runWriteCommandAction(project) {
        val text = document.text
        val lineStart = text.indexOf("function greet")
        val lineEnd = text.indexOf("\n", lineStart) + 1
        document.deleteString(lineStart, lineEnd)
      }

      waitForDiagnosticsFromLspServer(project, file.virtualFile)

      // Using doHighlighting() instead of checkLspHighlightingForData() intentionally:
      // checkLspHighlightingForData() retries up to 3 times on failure, which masks the bug
      // where Volar's multi-phase publishDiagnostics leaves stale data in the cache.
      // doHighlighting() runs once without retry, so it fails if the diagnostics restart
      // mechanism in AstroLspTypeScriptService is broken.
      val errors = doHighlighting(HighlightSeverity.ERROR)
      assertTrue("Expected an error on 'greet' after removing its declaration, but got: $errors",
                 errors.any { it.description?.contains("greet") == true })
    }
  }
}
