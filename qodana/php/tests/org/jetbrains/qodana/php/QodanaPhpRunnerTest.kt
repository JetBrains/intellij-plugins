package org.jetbrains.qodana.php

import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaPhpConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import org.junit.Test

@TestDataPath("\$CONTENT_ROOT/testData/QodanaPhpRunnerTest")
class QodanaPhpRunnerTest : QodanaRunnerTestCase() {

  private fun configurePhpVersion(phpVersion: String = "8.0") {
    updateQodanaConfig {
      it.copy(php = QodanaPhpConfig(phpVersion))
    }
  }

  @Test
  fun testPhpConstructorStyle() {
    configurePhpVersion()
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:PhpConstructorStyleInspection"),
      )
    }

    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testPhpConstructorStyleOldPhp() {
    configurePhpVersion("5.3.0")
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:PhpConstructorStyleInspection"),
      )
    }

    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testVulnerablePhp() {
    configurePhpVersion()
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:PhpVulnerablePathsInspection"),
        disableSanityInspections = true,
        runPromoInspections = false
      )
    }
    runAnalysis()
    assertSarifResults()
  }
}
