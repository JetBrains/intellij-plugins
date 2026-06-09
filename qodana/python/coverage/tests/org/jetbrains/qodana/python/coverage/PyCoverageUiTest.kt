package org.jetbrains.qodana.python.coverage

import com.intellij.coverage.view.CoverageViewManager
import com.intellij.python.pro.coverage.PyCoverageEngine
import com.intellij.rt.coverage.data.LineCoverage
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.coverage.CHANGED_LINES_ARTIFACT_ID
import org.jetbrains.qodana.report.ReportMetadata
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageUiTestBase
import org.junit.Test

class PyCoverageUiTest : QodanaCoverageUiTestBase("PyCoverageInspectionTest") {

  @Test
  fun loadsRegularCoverageReport(): Unit = runBlocking {
    val metadata = mapOf<String, ReportMetadata>(
      PY_COVERAGE to coverageArtifact("py", "PyCoverageEngine.xml", PY_COVERAGE),
    )
    highlightCoverageReport(metadata)

    val bundle = manager.currentSuitesBundle
    assertNotNull("Coverage suite was not opened", bundle)
    assertTrue("Unexpected engine: ${bundle!!.coverageEngine}", bundle.coverageEngine is PyCoverageEngine)
    assertEquals(1, manager.activeSuites().size)
    assertNotNull("Coverage view was not created", CoverageViewManager.getInstance(project).getView(bundle))

    assertCoverageTree(bundle, """
      -sources
       -src
        FooCls.py
        another.py
       -tests
        FooTest.py
    """.trimIndent())

    openFileInEditor("src/FooCls.py")
    assertEquals(
      mapOf(
        1 to LineCoverage.FULL, 2 to LineCoverage.FULL, 3 to LineCoverage.FULL, 4 to LineCoverage.FULL,
        5 to LineCoverage.NONE, 6 to LineCoverage.NONE, 7 to LineCoverage.NONE, 8 to LineCoverage.NONE,
        9 to LineCoverage.NONE, 10 to LineCoverage.NONE,
        11 to LineCoverage.FULL, 12 to LineCoverage.FULL, 13 to LineCoverage.FULL, 14 to LineCoverage.FULL,
        15 to LineCoverage.NONE, 16 to LineCoverage.NONE, 17 to LineCoverage.NONE, 18 to LineCoverage.NONE,
        19 to LineCoverage.NONE,
        20 to LineCoverage.FULL, 21 to LineCoverage.FULL, 22 to LineCoverage.FULL,
        23 to LineCoverage.NONE, 24 to LineCoverage.NONE, 25 to LineCoverage.NONE, 26 to LineCoverage.NONE,
        27 to LineCoverage.NONE, 28 to LineCoverage.NONE,
        29 to LineCoverage.FULL, 30 to LineCoverage.NONE, 31 to LineCoverage.NONE, 32 to LineCoverage.NONE,
        33 to LineCoverage.FULL, 34 to LineCoverage.NONE, 35 to LineCoverage.NONE, 36 to LineCoverage.NONE,
        37 to LineCoverage.NONE,
        38 to LineCoverage.FULL, 39 to LineCoverage.FULL, 40 to LineCoverage.FULL,
        41 to LineCoverage.FULL, 42 to LineCoverage.FULL,
        43 to LineCoverage.NONE, 44 to LineCoverage.NONE, 45 to LineCoverage.NONE,
      ),
      gutterCoverage("src/FooCls.py"),
    )
    openFileInEditor("src/another.py")
    assertEquals(
      mapOf(1 to LineCoverage.FULL, 2 to LineCoverage.NONE, 3 to LineCoverage.NONE),
      gutterCoverage("src/another.py"),
    )
    openFileInEditor("tests/FooTest.py")
    assertEquals(
      mapOf(
        1 to LineCoverage.FULL, 2 to LineCoverage.FULL, 5 to LineCoverage.FULL,
        7 to LineCoverage.FULL, 8 to LineCoverage.FULL, 9 to LineCoverage.FULL,
        10 to LineCoverage.FULL, 11 to LineCoverage.FULL,
      ),
      gutterCoverage("tests/FooTest.py"),
    )
  }

  @Test
  fun loadsIncrementalCoverageReport(): Unit = runBlocking {
    val metadata = mapOf(
      PY_COVERAGE to coverageArtifact("incrementalSecondStage", "PyCoverageEngine.xml", PY_COVERAGE),
      CHANGED_LINES_ARTIFACT_ID to changedLinesArtifact("incrementalSecondStage"),
    )
    highlightCoverageReport(metadata)

    val bundle = manager.currentSuitesBundle
    assertNotNull("Coverage suite was not opened", bundle)
    assertNotNull("Coverage view was not created", CoverageViewManager.getInstance(project).getView(bundle!!))

    assertCoverageTree(bundle, """
      -sources
       -src
        FooCls.py
    """.trimIndent())

    openFileInEditor("src/FooCls.py")
    assertEquals(
      mapOf(11 to LineCoverage.FULL, 12 to LineCoverage.FULL, 13 to LineCoverage.FULL),
      gutterCoverage("src/FooCls.py"),
    )

    openFileInEditor("src/another.py")
    assertTrue(
      "src/another.py should not be highlighted in incremental report",
      gutterCoverage("src/another.py").isNullOrEmpty()
    )

    openFileInEditor("tests/FooTest.py")
    assertTrue(
      "tests/FooTest.py should not be highlighted in incremental report",
      gutterCoverage("tests/FooTest.py").isNullOrEmpty()
    )
  }
}
