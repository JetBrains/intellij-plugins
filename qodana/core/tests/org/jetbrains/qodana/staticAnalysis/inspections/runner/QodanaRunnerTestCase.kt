package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.GlobalInspectionTool
import com.intellij.codeInspection.InspectionEP
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.LocalInspectionEP
import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.extensions.DefaultPluginDescriptor
import com.intellij.openapi.extensions.ExtensionPoint
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiDocumentManager
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaTestManager
import com.intellij.testFramework.HeavyTestHelper.createTestProjectStructure
import com.intellij.testFramework.JavaPsiTestCase
import com.jetbrains.qodana.sarif.model.Result
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

/**
 * Provides the infrastructure for running Qodana on a test project, verifying the SARIF results.
 *
 * The test project consists of a single module, which has the relative path `test-module`.
 * When configuring `include` or `exclude` sections via [updateQodanaConfig],
 * the paths typically start with this prefix.
 *
 * When setting up the test project, the test data is taken from the `testData` directory.
 * The subdirectory can be configured per test, using [testDataBasePath].
 * This subdirectory contains the following files and directories:
 *
 * | File/directory                  | Purpose                                             |
 * | ------------------------------- | --------------------------------------------------- |
 * | `sources`                       | the contents of the module in `test-module`         |
 * | `inspection-profile.xml`        | the inspection profile, with arbitrary `myName`     |
 * | `baseline-results.sarif.json`   | the (optional) result of the baseline run           |
 * | `expected.sarif.json`           | the expected SARIF output, for [assertSarifResults] |
 * | `qodana.sanity.xml`             | the (optional) sanity inspection profile            |
 * | `qodana.recommended.full.xml`   | the (optional) recommended inspection profile       |
 * | `qodana.starter.full.xml`       | the (optional) starter inspection profile           |
 */
@RunWith(JUnit4::class)
abstract class QodanaRunnerTestCase : JavaPsiTestCase() {
  protected val manager: QodanaTestManager = QodanaTestManager()

  lateinit var qodanaConfig: QodanaConfig private set

  protected val outputBasePath: Path = FileUtil.generateRandomTemporaryPath().toPath()

  protected fun qodanaRunner() = manager.qodanaRunner

  override fun runInDispatchThread(): Boolean = false

  override fun setUp() {
    super.setUp()
    invokeAndWaitIfNeeded {
      createTestStructure()
      val managerTestData = QodanaTestManager.TestData(
        project,
        testRootDisposable,
        Paths.get(project.basePath!!),
        outputBasePath,
        ::getTestDataPath
      )

      qodanaConfig = manager.setUp(managerTestData)
    }
  }

  override fun tearDown() {
    invokeAndWaitIfNeeded {
      super.tearDown()
    }
  }

  protected open fun createTestStructure() {
    val dir = Paths.get(project.basePath!!, "test-module")
    Files.createDirectories(dir)
    val sourceRoot = getTestDataPath("sources")
    if (sourceRoot.exists()) {
      createTestProjectStructure(myModule, sourceRoot.toString(), dir, true)
    }
    PsiDocumentManager.getInstance(myProject).commitAllDocuments()
  }

  fun runAnalysis() {
    manager.runAnalysis(project)
  }

  @Suppress("RAW_RUN_BLOCKING")
  protected fun loadInspectionProfile(): LoadedProfile = runBlocking {
    manager.loadInspectionProfile(project)
  }

  protected val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "core", "test-data")

  /** Returns the path to the test data, relative to the `testData` directory. */
  protected open val testDataBasePath: Path get() = Path.of(javaClass.simpleName, getTestName(true))

  /** Resolves a path in the subdirectory of `testData` that corresponds to this test case. */
  protected fun getTestDataPath(relativePath: String): Path = testData.resolve(testDataBasePath).resolve(relativePath)

  fun updateQodanaConfig(configure: (config: QodanaConfig) -> QodanaConfig) {
    val (config, _) = manager.updateQodanaConfig(Paths.get(project.basePath!!), outputBasePath, configure)
    qodanaConfig = config
  }

  /** Run an external command in the base path of the project, ensuring that it exits successfully. */
  protected fun run(vararg args: String) {
    val process = ProcessBuilder(*args)
      .directory(File(project.basePath!!))
      .redirectOutput(ProcessBuilder.Redirect.INHERIT)
      .redirectError(ProcessBuilder.Redirect.INHERIT)
      .start()
    process.outputStream.close()
    val exitStatus = process.waitFor()
    if (exitStatus != 0) throw IOException("""Command "${args.joinToString(" ") { "'$it'" }}" exited with status $exitStatus""")
  }

  /**
   * When [QodanaConfig.outputFormat] is set to [OutputFormat.INSPECT_SH_FORMAT],
   * each inspection creates a separate file for its results, in an IDEA-specific format.
   * The SARIF report is then generated from these files.
   */
  protected fun assertInspectionFilesInOutPath(vararg expected: String) {
    val actual = Files.list(outputBasePath).use { entries ->
      entries
        .filter { it.isRegularFile() }
        .map { it.fileName.toString() }
        .filter { it != ".descriptions.json" && !it.endsWith(".sarif.json") }
        .toList()
    }

    assertEquals(expected.toSortedSet(), actual.toSortedSet())
  }

  protected fun assertSarifExitCode(expected: Int) {
    assertEquals(expected, qodanaRunner().sarifRun.invocations[0].exitCode)
  }

  protected fun assertSarifEnabledRules(vararg expectedRulesIds: String) {
    val allRules = qodanaRunner().sarifRun.tool.extensions.flatMap { it.rules }
    val enabledRules = allRules.filter { it.defaultConfiguration.enabled }
    assertEquals(expectedRulesIds.toSortedSet(), enabledRules.map { it.id }.toSortedSet())
  }

  protected fun assertNoSarifResults() {
    assertEquals(null, qodanaRunner().sarifRun.results)
  }

  /**
   * Assert that the generated SARIF output matches `expected.sarif.json` from the `testData` directory.
   * Before actually comparing the files, the SARIF results are sorted by their location,
   * as Qodana writes them out in the order of appearance, to avoid having to keep all results in memory,
   * as there are projects that generate 100_000 results.
   */
  fun assertSarifResults() {
    val (actualJson, expectedSarif) = manager.computeSarifResult(::getTestDataPath)
    assertSameLinesWithFile(expectedSarif, actualJson)
  }

  /**
   * Assert that the SARIF output contains exactly the given problem markers,
   * each in the form "test-module/A.java:5:9: Human-readable message".
   */
  protected fun assertSarifSummary(vararg expectedEntries: String) {
    val comparator = compareBy<Result> { 0 }
      .thenBy { it.locations[0].physicalLocation.artifactLocation.uri }
      .thenBy { it.locations[0].physicalLocation.region.startLine }
      .thenBy { it.locations[0].physicalLocation.region.charOffset }

    val results = qodanaRunner().sarifRun.results.sortedWith(comparator)
    val actualEntries = results.map { result ->
      val loc = result.locations[0].physicalLocation
      val locStr = "${loc.artifactLocation.uri}:${loc.region.startLine}:${loc.region.startColumn}"
      "$locStr: ${result.message.text}"
    }

    assertEquals(expectedEntries.joinToString("\n"), actualEntries.joinToString("\n"))
  }

  protected fun registerTool(tool: InspectionProfileEntry) {
    val inspection = LocalInspectionEP().apply {
      pluginDescriptor = DefaultPluginDescriptor(PluginId.getId("qodanaTest"), javaClass.getClassLoader())
    }
    register(tool, inspection, LocalInspectionEP.LOCAL_INSPECTION.point)
  }

  protected fun registerGlobalTool(tool: GlobalInspectionTool) {
    val inspection = InspectionEP(
      tool.javaClass.canonicalName,
      DefaultPluginDescriptor(PluginId.getId("qodanaTest"), javaClass.getClassLoader())
    )

    register(tool, inspection, InspectionEP.GLOBAL_INSPECTION.point)
  }

  private fun <T : InspectionEP> register(
    tool: InspectionProfileEntry,
    emptyInspection: T,
    extensionPoint: ExtensionPoint<T>
  ) {
    emptyInspection.apply {
      shortName = tool.shortName
      displayName = tool.displayName
      groupDisplayName = tool.groupDisplayName
      level = "WARNING"
      enabledByDefault = true
      implementationClass = tool.javaClass.canonicalName
      groupPath = tool.groupPath.joinToString(",")
    }

    extensionPoint.registerExtension(emptyInspection, testRootDisposable)
  }

  fun assertOpenInIdeJson() {
    val expected = getTestDataPath("expected.open-in-ide.json")
    val actual = qodanaConfig.outPath.resolve("open-in-ide.json")
    assertSameLinesWithFile(expected.toString(), actual.readText())
  }
}
