package org.jetbrains.qodana.php

import com.intellij.openapi.application.PluginPathManager
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.inspections.config.FixesStrategy
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaPhpConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText

@TestDataPath($$"$CONTENT_ROOT/../test-data/QodanaPhpCleanupFixesTest")
class QodanaPhpCleanupFixesTest : QodanaRunnerTestCase() {
  override val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "php", "test-data")

  @Test
  fun testFullyQualifiedNamesCleanupIsStableAcrossRuns() {
    runCleanupTwiceAndAssertSingleRunCleanup()
  }

  @Test
  fun testFullyQualifiedNamesCleanupAcrossNamespaces() {
    runCleanupTwiceAndAssertSingleRunCleanup()
  }

  private fun runCleanupTwiceAndAssertSingleRunCleanup() {
    updateQodanaConfig {
      it.copy(
        fixesStrategy = FixesStrategy.CLEANUP,
        profile = QodanaProfileConfig.fromPath(getTestDataPath("inspection-profile.xml").absolutePathString()),
        php = QodanaPhpConfig("8.0"),
        disableSanityInspections = true,
        runPromoInspections = false,
      )
    }
    runBeforeAnalysis { config, project ->
      QodanaUpdatePhpLanguageLevel().configureForQodana(config, project)
    }

    val containerFile = Path.of(project.basePath!!, "test-module", "container.php")
    val expectedAfterCleanup = getTestDataPath("afterFixes/container.php").readText()

    runAnalysis()

    val textAfterFirstRun = containerFile.readText()
    assertSameLines(expectedAfterCleanup, textAfterFirstRun)

    runAnalysis()

    assertEmpty(manager.sarifRun.results)
    assertSarifResults()
    assertSameLines(textAfterFirstRun, containerFile.readText())
  }
}
