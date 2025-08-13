package org.jetbrains.qodana.jvm.java

import com.intellij.openapi.application.PluginPathManager
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase.Companion.runTest
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

@TestDataPath($$"$CONTENT_ROOT/test-data/QodanaJavaSanityTest")
class QodanaJavaSanityTest : QodanaRunnerTestCase() {
  override val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "jvm", "java", "test-data")

  @Test
  fun `testUnresolved references`(): Unit = runTest {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:ConstantValue"),
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
        profile = QodanaProfileConfig.named("qodana.single:ConstantValue"),
        disableSanityInspections = false
      )
    }
    runAnalysis()
    assertSarifResults()
  }
}