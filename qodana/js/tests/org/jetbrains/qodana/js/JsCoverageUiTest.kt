package org.jetbrains.qodana.js

import com.intellij.coverage.view.CoverageViewManager
import com.intellij.javascript.testing.coverage.jest.JestCoverageEngine
import com.intellij.openapi.application.ApplicationManager
import com.intellij.rt.coverage.data.LineCoverage
import com.intellij.testFramework.common.ThreadLeakTracker
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.coverage.CHANGED_LINES_ARTIFACT_ID
import org.jetbrains.qodana.report.ReportMetadata
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageUiTestBase
import org.junit.Test

class JsCoverageUiTest : QodanaCoverageUiTestBase("JsCoverageInspectionTest") {

  override fun setUp() {
    super.setUp()
    // WipRemoteVmConnection defaults to thread pool created in companion object in unit testing, which results in leak
    ThreadLeakTracker.longRunningThreadCreated(ApplicationManager.getApplication(), "WipRemoteVmConnection")
  }

  @Test
  fun loadsRegularCoverageReport(): Unit = runBlocking {
    val metadata = mapOf<String, ReportMetadata>(
      JEST_COVERAGE to coverageArtifact("jest", "JestCoverageEngine.info", JEST_COVERAGE),
    )
    highlightCoverageReport(metadata)

    val bundle = manager.currentSuitesBundle
    assertNotNull("Coverage suite was not opened", bundle)
    assertTrue("Unexpected engine: ${bundle!!.coverageEngine}", bundle.coverageEngine is JestCoverageEngine)
    assertEquals(1, manager.activeSuites().size)

    // The cloud artifact stores project-relative paths; they must be remapped to absolute paths under the project.
    val projectDir = projectBasePath()
    val nonAbsoluteClass = bundle.coverageData!!.classes.keys.find { !it.startsWith(projectDir) }
    assertTrue("Expected an absolute paths, got $nonAbsoluteClass", nonAbsoluteClass == null)

    // The tool window view is created and activated through QodanaCoverageToolWindowActivator.
    assertNotNull("Coverage view was not created", CoverageViewManager.getInstance(project).getView(bundle))

    // Full report: both covered files are shown in the tool window tree.
    assertCoverageTree(bundle, """
      -sources
       FooCls.ts
       javascript.js
    """.trimIndent())

    // Full report: every executable line of FooCls.ts is painted in the gutter.
    openFileInEditor("FooCls.ts")
    assertEquals(
      mapOf(
        7 to LineCoverage.FULL, 8 to LineCoverage.NONE, 10 to LineCoverage.FULL, 15 to LineCoverage.FULL,
        19 to LineCoverage.FULL, 20 to LineCoverage.FULL, 21 to LineCoverage.FULL, 23 to LineCoverage.FULL,
        27 to LineCoverage.NONE, 28 to LineCoverage.NONE, 30 to LineCoverage.NONE, 31 to LineCoverage.NONE,
        32 to LineCoverage.NONE, 35 to LineCoverage.FULL, 36 to LineCoverage.NONE, 42 to LineCoverage.FULL,
        44 to LineCoverage.NONE, 45 to LineCoverage.NONE, 51 to LineCoverage.NONE, 55 to LineCoverage.FULL,
        64 to LineCoverage.NONE, 65 to LineCoverage.NONE, 67 to LineCoverage.NONE,
      ),
      gutterCoverage("FooCls.ts"),
    )

    openFileInEditor("javascript.js")
    assertEquals(
      mapOf(1 to LineCoverage.FULL, 2 to LineCoverage.FULL, 3 to LineCoverage.NONE, 4 to LineCoverage.NONE, 5 to LineCoverage.NONE),
      gutterCoverage("javascript.js"),
    )
  }

  @Test
  fun loadsIncrementalCoverageReport(): Unit = runBlocking {
    val metadata = mapOf(
      JEST_COVERAGE to coverageArtifact("incrementalSecondStage", "JestCoverageEngine.info", JEST_COVERAGE),
      CHANGED_LINES_ARTIFACT_ID to changedLinesArtifact("incrementalSecondStage"),
    )
    highlightCoverageReport(metadata)

    val bundle = manager.currentSuitesBundle
    assertNotNull("Coverage suite was not opened", bundle)
    assertEquals(1, manager.activeSuites().size)
    assertNotNull("Coverage view was not created", CoverageViewManager.getInstance(project).getView(bundle!!))

    // Incremental report: data is filtered to the changed lines, so only FooCls.ts is shown in the tool window tree.
    assertCoverageTree(bundle, """
      -sources
       FooCls.ts
    """.trimIndent())

    // Incremental report: only the changed lines (19-21) of FooCls.ts are painted in the gutter.
    openFileInEditor("FooCls.ts")
    assertEquals(
      mapOf(19 to LineCoverage.FULL, 20 to LineCoverage.FULL, 21 to LineCoverage.FULL),
      gutterCoverage("FooCls.ts"),
    )
  }
}
