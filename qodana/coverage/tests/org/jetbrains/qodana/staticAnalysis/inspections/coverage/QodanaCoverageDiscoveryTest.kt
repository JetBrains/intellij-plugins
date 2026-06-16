package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.openapi.observable.util.whenDisposed
import com.intellij.openapi.util.io.NioFiles
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.COVERAGE_DATA
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * One fixture is shared by every location case of a class, under `<Class>/`:
 * - `project/` — the minimal project **without** any report;
 * - `report.<ext>` — the single report fixture;
 * - `expected.sarif.json` — the golden SARIF (identical for every location, since the same report is loaded).
 *
 * Each [Case] is a parameterized run: the project is copied to a fresh temp directory and the report is dropped
 * at the case's [Case.placements] before the project is opened.
 */
abstract class QodanaCoverageDiscoveryTest(
  inspection: String,
  private val case: Case,
) : QodanaCoverageInspectionTest(inspection) {

  override val testDataBasePath: Path get() = Path.of(javaClass.simpleName)

  override fun getTestName(lowercaseFirstLetter: Boolean): String = case.name

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
    for ((fixture, location) in case.placements) {
      val report = projectRoot.resolve(location)
      report.parent?.createDirectories()
      Files.copy(getTestDataPath(fixture), report)
    }
    return projectRoot
  }

  protected fun runDiscovery() {
    val preciousCoverageDataValue = System.getProperty(COVERAGE_DATA)
    try {
      System.clearProperty(COVERAGE_DATA)
      runUnderCoverDataInSources()
      assertSarifResults()
    } finally {
        preciousCoverageDataValue?.let { System.setProperty(COVERAGE_DATA, it) } ?: System.clearProperty(COVERAGE_DATA)
    }
  }

  data class ReportLocation(
    val testDataLocation: String,
    val projectReportLocation: String
  )

  data class Case(val name: String, val placements: List<ReportLocation>) {
    constructor(name: String, vararg placements: ReportLocation) : this(name, placements.toList())

    override fun toString(): String = name
  }
}
