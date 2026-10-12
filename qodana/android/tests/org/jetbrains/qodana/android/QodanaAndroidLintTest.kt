package org.jetbrains.qodana.android

import com.android.tools.idea.util.CommonAndroidUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.replaceService
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase.Companion.runTest
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Path

/**
 * Runs Android Lint through the Qodana runner and checks that the SARIF report contains the Lint problems.
 *
 * Qodana gives a `CUSTOM` [org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope] to the inspections,
 * and it calls them without a read action.
 * `LintGlobalInspectionContext` finds the modules of this scope through `QodanaAnalysisScope.containsModule` (QD-11439).
 * If Lint fails there, Qodana drops all Android Lint problems and still writes a valid SARIF report with the exit code 0.
 */
class QodanaAndroidLintTest : QodanaRunnerTestCase() {
  override val testData: Path = Path.of(PluginPathManager.getPluginHomePath("qodana"), "android", "test-data")

  override val testDataBasePath: Path = Path.of(javaClass.simpleName)

  override fun setUp() {
    super.setUp()
    // Android Lint analyzes only an Android project in the batch mode.
    ApplicationManager.getApplication().replaceService(CommonAndroidUtil::class.java, object : CommonAndroidUtil() {
      override fun isAndroidProject(project: Project): Boolean = true
    }, testRootDisposable)
  }

  @Test
  fun `test project scope`(): Unit = runTest {
    updateQodanaConfig {
      it.copy(profile = QodanaProfileConfig.named("qodana.single:AndroidLintUseValueOf"))
    }
    runAnalysis()
    assertUseValueOfResult()
  }

  @Test
  fun `test directory scope`(): Unit = runTest {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:AndroidLintUseValueOf"),
        onlyDirectory = Path.of("test-module"),
      )
    }
    runAnalysis()
    assertUseValueOfResult()
  }

  private fun assertUseValueOfResult() {
    assertSarifExitCode(0)
    assertSarifResultLocations("test-module/p1/p2/Boxing.java:5")
    assertEquals(listOf("AndroidLintUseValueOf"), manager.sarifRun.results.map { it.ruleId })
  }
}
