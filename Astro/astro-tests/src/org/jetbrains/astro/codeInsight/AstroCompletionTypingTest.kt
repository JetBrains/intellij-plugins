package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.AstroTestModule
import org.junit.jupiter.api.Test

@TestApplication
class AstroCompletionTypingTest : AstroCodeInsightTestCase("codeInsight/completion") {
  @Test
  fun testImportComponent() =
    doTypingTest("Compo\n", additionalFiles = listOf("component.astro"))

  @Test
  fun testImportExternalSymbolFrontmatter() =
    doTypingTest("olo\n", additionalFiles = listOf("colors.ts"))

  @Test
  fun testImportExternalSymbolExpression() =
    doTypingTest("olo\n", additionalFiles = listOf("colors.ts"))

  @Test
  fun testImportWithinScriptBlock() =
    doTypingTest("getRandomNumber\n", additionalFiles = listOf("functions.ts"))

  @Test
  fun testFrontmatterKeywords() =
    astro.doLookupTest(additionalFiles = listOf("component.astro")) {
      it.priority.toInt() == JSLookupPriority.KEYWORDS_PRIORITY.priorityValue
      || it.priority.toInt() == JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
    }

  //region Test configuration and helper methods

  private fun doTypingTest(textToType: String,
                           additionalFiles: List<String> = emptyList(),
                           vararg modules: AstroTestModule) {
    astro.doConfiguredTest(additionalFiles = additionalFiles, modules = modules, checkResult = true) {
      completeBasic()
      type(textToType)
    }
  }

  //endregion

}