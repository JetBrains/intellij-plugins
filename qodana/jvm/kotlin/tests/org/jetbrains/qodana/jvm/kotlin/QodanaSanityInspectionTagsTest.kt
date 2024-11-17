package org.jetbrains.qodana.jvm.kotlin

import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase.Companion.runTest
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import org.junit.Test

@TestDataPath("/QodanaSanityInspectionTagsTest")
class QodanaSanityInspectionTagsTest : QodanaRunnerTestCase() {

  @Test
  fun `testUnresolved imports`(): Unit = runTest {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:QodanaSanity"),
        fileSuspendThreshold = Int.MAX_VALUE,
        moduleSuspendThreshold = Int.MAX_VALUE,
        projectSuspendThreshold = Int.MAX_VALUE
      )
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun `testUnresolved types`(): Unit = runTest {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:QodanaSanity"),
        fileSuspendThreshold = Int.MAX_VALUE,
        moduleSuspendThreshold = Int.MAX_VALUE,
        projectSuspendThreshold = Int.MAX_VALUE
      )
    }
    runAnalysis()
    assertSarifResults()
  }
}