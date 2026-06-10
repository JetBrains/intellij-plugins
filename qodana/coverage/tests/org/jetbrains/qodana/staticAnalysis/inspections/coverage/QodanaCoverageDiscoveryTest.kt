package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.openapi.observable.util.whenDisposed
import com.intellij.openapi.util.io.NioFiles
import com.intellij.testFramework.RunAll
import com.intellij.util.ThrowableRunnable
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * One fixture is shared by every location method of a class, under `<Class>/`:
 * - `project/` — the minimal project **without** any report;
 * - `report.<ext>` — the single report fixture;
 * - `expected.sarif.json` — the golden SARIF (identical for every location, since the same report is loaded).
 *
 * For each test method the project is copied to a fresh temp directory and the report is dropped at the
 * method's [reportPlacements] before the project is opened
 */
abstract class QodanaCoverageDiscoveryTest(inspection: String) : QodanaCoverageInspectionTest(inspection) {

  override val testDataBasePath: Path get() = Path.of(javaClass.simpleName)

  /**
   * Return information about where place each coverage report in the analysed project
   */
  protected abstract fun reportPlacements(testName: String): List<ReportLocation>

  /**
   * Sub-path under the temp root at which the project fixture is laid out
   */
  protected open val projectDirName: String get() = "project"

  override fun getProjectSourcesPath(): Path {
    val root = Files.createTempDirectory("qodanaCoverageDiscovery")
    testRootDisposable.whenDisposed { NioFiles.deleteRecursively(root) }
    val projectFixture = testData.resolve(testDataBasePath).resolve("project")
    val target = root.resolve(projectDirName)
    target.parent?.createDirectories()
    NioFiles.copyRecursively(projectFixture, target)
    val projectRoot = target.toRealPath()
    for ((fixture, location) in reportPlacements(getTestName(true))) {
      val report = projectRoot.resolve(location)
      report.parent?.createDirectories()
      Files.copy(getTestDataPath(fixture), report)
    }
    return projectRoot
  }

  protected fun runDiscovery() {
    runUnderCoverDataInSources()
    assertSarifResults()
  }

  data class ReportLocation(
    val testDataLocation: String,
    val projectReportLocation: String
  )
}
