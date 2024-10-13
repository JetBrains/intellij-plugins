package org.jetbrains.qodana.jvm.java

import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase.Companion.runTest
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import org.junit.Test

@TestDataPath("\$CONTENT_ROOT/../../core/test-data/QodanaJavaSanityTest")
class QodanaJavaSanityTest : QodanaRunnerTestCase() {

  @Test
  fun `testUnresolved references`(): Unit = runTest {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:ConstantValue"),
        disableSanityInspections = false
      )
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun `testUnresolved references in javadoc`(): Unit = runTest {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:ConstantValue"),
        disableSanityInspections = false
      )
    }
    runAnalysis()
    assertSarifResults()
  }
}