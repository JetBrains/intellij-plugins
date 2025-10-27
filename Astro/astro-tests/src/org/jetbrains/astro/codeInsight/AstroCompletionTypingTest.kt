package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.completion.JSLookupPriority
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.AstroTestModule

class AstroCompletionTypingTest : AstroCodeInsightTestCase("codeInsight/completion") {
  fun testImportComponent() =
    doTypingTest("Compo\n", additionalFiles = listOf("component.astro"))

  fun testImportExternalSymbolFrontmatter() =
    doTypingTest("olo\n", additionalFiles = listOf("colors.ts"))

  fun testImportExternalSymbolExpression() =
    doTypingTest("olo\n", additionalFiles = listOf("colors.ts"))

  fun testImportWithinScriptBlock() =
    doTypingTest("getRandomNumber\n", additionalFiles = listOf("functions.ts"))

  fun testFrontmatterKeywords() =
    doLookupTest(additionalFiles = listOf("component.astro")) {
      it.priority.toInt() == JSLookupPriority.KEYWORDS_PRIORITY.priorityValue
      || it.priority.toInt() == JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
    }

  //region Test configuration and helper methods

  private fun doTypingTest(textToType: String,
                           additionalFiles: List<String> = emptyList(),
                           vararg modules: AstroTestModule) {
    doConfiguredTest(additionalFiles = additionalFiles, modules = modules, checkResult = true) {
      completeBasic()
      type(textToType)
    }
  }

  //endregion

}