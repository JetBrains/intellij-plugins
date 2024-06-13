package org.jetbrains.astro

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.javascript.BaseJSCompletionTestCase
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import java.io.File

private const val ASTRO_TEST_DATA_PATH = "/Astro/astro-tests/testData"

fun getAstroTestDataPath(): String =
  getContribPath() + ASTRO_TEST_DATA_PATH

fun getRelativeAstroTestDataPath(): String = "/contrib$ASTRO_TEST_DATA_PATH"

internal fun Array<LookupElement>.checkCompletionContains(vararg variants: String) = BaseJSCompletionTestCase.checkWeHaveInCompletion(this, *variants)

private fun getContribPath(): String {
  val homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy()
  return if (File(homePath, "contrib/.gitignore").isFile) {
    homePath + File.separatorChar + "contrib"
  }
  else homePath
}