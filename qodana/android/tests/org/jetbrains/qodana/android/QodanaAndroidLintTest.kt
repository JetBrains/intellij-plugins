package org.jetbrains.qodana.android

import com.android.tools.idea.util.CommonAndroidUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.replaceService
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase.Companion.runTest
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Path

@TestDataPath($$"$CONTENT_ROOT/../test-data/QodanaAndroidLintTest")
class QodanaAndroidLintTest : QodanaRunnerTestCase() {
  override val testData: Path = Path.of(PluginPathManager.getPluginHomePath("qodana"), "android", "test-data")

  override val testDataBasePath: Path = Path.of(javaClass.simpleName)

  override fun setUp() {
    super.setUp()
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
    assertUseValueOfResults("test-module/excluded/Boxing.java:5", "test-module/included/Boxing.java:5")
  }

  @Test
  fun `test directory scope`(): Unit = runTest {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:AndroidLintUseValueOf"),
        onlyDirectory = Path.of("test-module", "included"),
      )
    }
    runAnalysis()
    assertUseValueOfResults("test-module/included/Boxing.java:5")
  }

  private fun assertUseValueOfResults(vararg expectedLocations: String) {
    assertSarifExitCode(0)
    assertSarifResultLocations(*expectedLocations)
    assertEquals(List(expectedLocations.size) { "AndroidLintUseValueOf" }, manager.sarifRun.results.map { it.ruleId })
  }
}
