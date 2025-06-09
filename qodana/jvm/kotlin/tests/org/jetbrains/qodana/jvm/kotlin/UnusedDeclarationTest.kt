package org.jetbrains.qodana.jvm.kotlin

import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import org.junit.Test

@TestDataPath("/UnusedDeclarationTest")
class UnusedDeclarationTest : QodanaRunnerTestCase() {
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