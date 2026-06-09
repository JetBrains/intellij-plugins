package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.JavaModuleTestCase
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.rt.coverage.data.LineData
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.util.ProjectDataLoader
import org.jetbrains.qodana.coverage.CHANGED_LINES_FILE_NAME
import org.jetbrains.qodana.coverage.readChangedLinesPayload
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaScriptConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.COVERAGE_DATA
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.jetbrains.qodana.staticAnalysis.script.scoped.SCOPED_SCRIPT_NAME
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaTestManager
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@RunWith(JUnit4::class)
abstract class QodanaCoverageInspectionTest(val inspection: String): JavaModuleTestCase() {
  private val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "core", "test-data")
  private lateinit var manager: QodanaTestManager
  private val outputBasePath: Path = FileUtil.generateRandomTemporaryPath().toPath()
  private val testDataBasePath: Path get() = Path.of(javaClass.simpleName, getTestName(true))
  protected lateinit var qodanaConfig: QodanaConfig

  override fun setUpProject() {
    manager = QodanaTestManager()
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

  protected fun getTestDataPath(relativePath: String): Path = testData.resolve(testDataBasePath).resolve(relativePath)

  private fun getProfileConfig(relativePath: String): QodanaProfileConfig {
    return QodanaProfileConfig.fromPath(getTestDataPath(relativePath).toString())
  }

  protected fun assertSarifResults() {
    val (actualJson, expectedSarif) = manager.computeSarifResult(::getTestDataPath)
    assertSameLinesWithFile(expectedSarif, actualJson)
  }

  protected fun runUnderCoverDataInSources(customProfile: String? = null) {
    val customProfileConfig = customProfile?.let { getProfileConfig(it) }
    val (config, _) = manager.updateQodanaConfig(Paths.get(myProject.basePath!!), outputBasePath) {
      it.copy(
        include = listOf(InspectScope(inspection)),
        profile = customProfileConfig ?: QodanaProfileConfig.named("empty")
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

  /**
   * Run the inspection through the scoped script over [scopeJson]
   */
  protected fun runIncrementalAnalysis(stage: QodanaCoverageComputationState, scopeJson: String) {
    require(stage.isIncrementalAnalysis()) { "Stage must be incremental, got $stage" }
    val skipProperty = if (stage.isFirstStage()) {
      "qodana.skip.coverage.computation"
    }
    else {
      "qodana.skip.coverage.issues.reporting"
    }
    val scopeFile = Files.createTempFile("qodana-scope", ".json")
    try {
      Files.writeString(scopeFile, scopeJson.trimIndent())
      System.setProperty(COVERAGE_DATA, testData.resolve(testDataBasePath).resolve("coverage").toString())
      System.setProperty(skipProperty, "true")
      val (config, _) = manager.updateQodanaConfig(Paths.get(myProject.basePath!!), outputBasePath) {
        it.copy(
          script = QodanaScriptConfig(SCOPED_SCRIPT_NAME, mapOf("scope-file" to scopeFile.toString())),
          profile = QodanaProfileConfig.named("qodana.single:$inspection"),
        )
      }
      qodanaConfig = config
      manager.runAnalysis(myProject)
    }
    finally {
      System.clearProperty(skipProperty)
      System.clearProperty(COVERAGE_DATA)
      Files.deleteIfExists(scopeFile)
    }
  }

  protected fun assertChangedLines(expected: Map<String, Set<Int>>) {
    val payload = readChangedLinesPayload(qodanaConfig.coverage.coveragePath.resolve(CHANGED_LINES_FILE_NAME))
    if (expected.isEmpty()) {
      assertNull("Expected no changed-lines artifact to be produced", payload)
      return
    }
    assertNotNull("Changed-lines artifact was not produced", payload)
    // Map and Set equality is order-insensitive, so this asserts the exact same content regardless of ordering.
    assertEquals(expected, payload!!.files)
  }

  /**
   * Assert that the serialized coverage [ProjectData] produced under [engineName] contains exactly the same
   * information as the golden stored in `artifacts/<goldenFileName>`. When the golden is missing it is created from
   * the produced artifact
   */
  protected fun assertCoverageProjectDataMatchesGolden(engineName: String, goldenFileName: String) {
    val actualFile = qodanaConfig.coverage.coveragePath.resolve(engineName)
    assertTrue("Coverage artifact '$engineName' was not produced at $actualFile", Files.isRegularFile(actualFile))

    val goldenFile = getTestDataPath("artifacts/$goldenFileName")
    if (!Files.isRegularFile(goldenFile)) {
      Files.createDirectories(goldenFile.parent)
      Files.copy(actualFile, goldenFile)
      fail("Golden coverage artifact was missing and has been created at $goldenFile. Re-run the test to validate it.")
    }

    val expected = ProjectDataLoader.load(goldenFile.toFile())
    val actual = ProjectDataLoader.load(actualFile.toFile())
    assertEquals(canonicalizeCoverage(expected), canonicalizeCoverage(actual))
  }

  protected fun assertChangedLinesMatchesGolden(goldenFileName: String = CHANGED_LINES_FILE_NAME) {
    val actualFile = qodanaConfig.coverage.coveragePath.resolve(CHANGED_LINES_FILE_NAME)
    assertTrue("Changed-lines artifact was not produced at $actualFile", Files.isRegularFile(actualFile))

    val goldenFile = getTestDataPath("artifacts/$goldenFileName")
    if (!Files.isRegularFile(goldenFile)) {
      Files.createDirectories(goldenFile.parent)
      Files.copy(actualFile, goldenFile)
      fail("Golden changed-lines artifact was missing and has been created at $goldenFile. Re-run the test to validate it.")
    }

    val expected = readChangedLinesPayload(goldenFile)
    val actual = readChangedLinesPayload(actualFile)
    assertNotNull("Golden changed-lines artifact could not be parsed: $goldenFile", expected)
    assertNotNull("Produced changed-lines artifact could not be parsed: $actualFile", actual)
    assertEquals(expected!!.files, actual!!.files)
  }

  private fun canonicalizeCoverage(data: ProjectData): Map<String, Map<Int, String>> {
    return data.classes.entries
      .sortedBy { it.key }
      .associate { (name, classData) ->
        @Suppress("UNCHECKED_CAST")
        val lines = (classData.lines as Array<LineData?>?)?.filterNotNull().orEmpty()
        name to lines
          .sortedBy { it.lineNumber }
          .associate { it.lineNumber to "hits=${it.hits};status=${it.status}" }
      }
  }
}