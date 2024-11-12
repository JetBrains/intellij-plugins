package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.UsefulTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.config.FixesStrategy
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.sarif.ALLOW_NON_BATCH_FIXES
import org.jetbrains.qodana.staticAnalysis.sarif.FixesLogger
import java.nio.file.Path
import org.junit.Test
import kotlin.io.path.absolutePathString

@TestDataPath("\$CONTENT_ROOT/testData/QodanaQuickFixesLoggingTest")
class QodanaQuickFixesLoggingTest: QodanaRunnerTestCase() {

  @Test
  fun cleanupGeneral() {
    run(FixesStrategy.CLEANUP)
    assertDefaultLogData()
  }

  @Test
  fun cleanupDiff() {
    runWithDiffEnabled {
      run(FixesStrategy.CLEANUP)
    }
    assertDiffLogData()
  }

  @Test
  fun applyModCommandGeneral() {
    run(FixesStrategy.APPLY)
    assertDefaultLogData()
  }

  @Test
  fun applyModCommandDiff() {
    runWithDiffEnabled {
      run(FixesStrategy.APPLY)
    }
    assertDiffLogData()
  }

  @Test
  fun applyNonModCommandGeneral() {
    runWithNonCommandEnabled {
      run(FixesStrategy.APPLY)
    }
    assertDefaultLogData()
  }

  @Test
  fun applyNonModCommandDiff() {
    runWithDiffEnabled {
      runWithDiffEnabled {
        run(FixesStrategy.APPLY)
      }
    }
    assertDiffLogData()
  }

  private fun run(strategy: FixesStrategy) {
    updateQodanaConfig {
      it.copy(
        fixesStrategy = strategy,
        disableSanityInspections = true,
        profile = QodanaProfileConfig(path = getTestDataPath("inspection-profile.yaml").absolutePathString())
      )
    }
    runAnalysis()
  }

  private fun runWithDiffEnabled(runnable: () -> Unit) {
    val previousValue: String? = System.getProperty(FixesLogger.INCLUDE_FIXES_DIFF_KEY)
    System.setProperty(FixesLogger.INCLUDE_FIXES_DIFF_KEY, "true")
    try {
      runnable()
    }
    finally {
      if (previousValue == null) {
        System.clearProperty(FixesLogger.INCLUDE_FIXES_DIFF_KEY)
      }
      else {
        System.setProperty(FixesLogger.INCLUDE_FIXES_DIFF_KEY, previousValue)
      }
    }
  }

  private fun runWithNonCommandEnabled(runnable: () -> Unit) {
    val previousValue: String? = System.getProperty(ALLOW_NON_BATCH_FIXES)
    System.setProperty(ALLOW_NON_BATCH_FIXES, "true")
    try {
      runnable()
    }
    finally {
      if (previousValue == null) {
        System.clearProperty(ALLOW_NON_BATCH_FIXES)
      }
      else {
        System.setProperty(ALLOW_NON_BATCH_FIXES, previousValue)
      }
    }
  }

  private fun assertDefaultLogData() {
    val logFile = Path.of(PathManager.getLogPath(), "qodana", "fixes.json").toFile().readText()
    val expectedLogFile = getTestDataPath("expected-fixes.json").toFile().readText()
    UsefulTestCase.assertSameLines(expectedLogFile, logFile)
  }

  private fun assertDiffLogData() {
    val logFile = Path.of(PathManager.getLogPath(), "qodana", "files-modifications.json").toFile().readText()
    val expectedLogFile = getTestDataPath("expected-diffs.json").toFile().readText()
    UsefulTestCase.assertSameLines(expectedLogFile, logFile)
  }
}