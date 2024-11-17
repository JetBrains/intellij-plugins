package org.jetbrains.qodana.jvm.coverage

import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaScriptConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.COVERAGE_DATA
import org.jetbrains.qodana.staticAnalysis.script.LocalChangesScriptBaseTest
import org.junit.Test

@TestDataPath("\$CONTENT_ROOT/testData/LocalChangesScriptTest")
class LocalChangesScriptTest : LocalChangesScriptBaseTest() {
  @Test
  fun coverage() {
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig("local-changes"),
      )
    }
    try {
      System.setProperty(COVERAGE_DATA, getTestDataPath("coverage").toString())
      runAnalysis()
      assertSarifResults()
    } finally {
      System.setProperty(COVERAGE_DATA, "")
    }
  }
}