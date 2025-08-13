package org.jetbrains.qodana.php

import com.intellij.openapi.application.PluginPathManager
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaPhpConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

@TestDataPath($$"$CONTENT_ROOT/test-data/QodanaPhpRunnerTest")
class QodanaPhpRunnerTest : QodanaRunnerTestCase() {
  override val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "php", "test-data")

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
        profile = QodanaProfileConfig.named("qodana.single:PhpConstructorStyleInspection"),
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
        profile = QodanaProfileConfig.named("qodana.single:PhpConstructorStyleInspection"),
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
        profile = QodanaProfileConfig.named("qodana.single:PhpVulnerablePathsInspection"),
        disableSanityInspections = true,
        runPromoInspections = false
      )
    }
    runAnalysis()
    assertSarifResults()
  }
}
