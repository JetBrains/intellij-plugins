package org.jetbrains.qodana.jvm.kotlin

import com.intellij.openapi.application.PluginPathManager
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

@TestDataPath($$"$CONTENT_ROOT/test-data/UnusedDeclarationTest")
class UnusedDeclarationTest : QodanaRunnerTestCase() {
  override val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "jvm", "kotlin", "test-data")

  @Test
  fun `testUnused declaration no kotlin findings`() {
    doTest()
  }

  @Test
  fun `testUnused declaration java findings`() {
    doTest()
  }

  private fun doTest() {
    setUpConfig()
    runAnalysis()
    assertSarifResults()
  }

  private fun setUpConfig() {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:unused"),
      )
    }
  }
}