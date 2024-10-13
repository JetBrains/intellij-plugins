package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaTestManager
import com.intellij.testFramework.JavaModuleTestCase
import com.intellij.testFramework.PlatformTestUtil
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.COVERAGE_DATA
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Path
import java.nio.file.Paths

@RunWith(JUnit4::class)
abstract class QodanaCoverageInspectionTest(val inspection: String): JavaModuleTestCase() {
  private val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "core", "test-data")
  private val manager: QodanaTestManager = QodanaTestManager()
  private val outputBasePath: Path = FileUtil.generateRandomTemporaryPath().toPath()
  private val testDataBasePath: Path get() = Path.of(javaClass.simpleName, getTestName(true))
  protected lateinit var qodanaConfig: QodanaConfig

  override fun setUpProject() {
    myProject = PlatformTestUtil.loadAndOpenProject(testData.resolve(javaClass.simpleName).resolve("sources"), testRootDisposable)
    val managerTestData = QodanaTestManager.TestData(
      myProject,
      testRootDisposable,
      Paths.get(myProject.basePath!!),
      outputBasePath,
      ::getTestDataPath
    )

    qodanaConfig = manager.setUp(managerTestData)
  }

  private fun getTestDataPath(relativePath: String): Path = testData.resolve(testDataBasePath).resolve(relativePath)

  private fun getProfileConfig(relativePath: String) = QodanaProfileConfig(getTestDataPath(relativePath).toString(), "")

  protected fun assertSarifResults() {
    val (actualJson, expectedSarif) = manager.computeSarifResult(::getTestDataPath)
    assertSameLinesWithFile(expectedSarif, actualJson)
  }

  protected fun runUnderCoverDataInSources(customProfile: String? = null) {
    val customProfileConfig = customProfile?.let { getProfileConfig(it) }
    val (config, _) = manager.updateQodanaConfig(Paths.get(myProject.basePath!!), outputBasePath) {
      it.copy(
        include = listOf(InspectScope(inspection)),
        profile = customProfileConfig ?: QodanaProfileConfig(name = "empty")
      )
    }
    qodanaConfig = config
    manager.runAnalysis(myProject)
  }

  protected fun runUnderCover(customProfile: String? = null) {
    try {
      System.setProperty(COVERAGE_DATA, testData.resolve(testDataBasePath).resolve("coverage").toString())
      runUnderCoverDataInSources(customProfile)
    }
    finally {
      System.clearProperty(COVERAGE_DATA)
    }
  }
}