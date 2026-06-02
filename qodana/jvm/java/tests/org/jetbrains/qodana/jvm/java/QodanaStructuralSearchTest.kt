package org.jetbrains.qodana.jvm.java

import com.intellij.openapi.application.PluginPathManager
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

@TestDataPath($$"$CONTENT_ROOT/../test-data/QodanaStructuralSearchTest")
class QodanaStructuralSearchTest : QodanaRunnerTestCase() {
  override val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "jvm", "java", "test-data")

  @Test
  fun testProfileWithDisabledRule() {
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testProfileWithDisabledRuleAndExclude() {
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testExcludePaths() {
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testExcludeByUUID() {
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testExcludePathByUUID() {
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testIncludeByUUID() {
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testIncludePathByUUID() {
    runAnalysis()
    assertSarifResults()
  }
}