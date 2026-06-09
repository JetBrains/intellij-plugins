package org.jetbrains.qodana.jvm.coverage

import com.intellij.coverage.JavaCoverageEngine
import com.intellij.coverage.view.CoverageViewManager
import com.intellij.rt.coverage.data.LineCoverage
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.coverage.CHANGED_LINES_ARTIFACT_ID
import org.jetbrains.qodana.report.ReportMetadata
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageUiTestBase
import org.junit.Test

class JvmCoverageUiTest : QodanaCoverageUiTestBase("JvmCoverageInspectionTest") {

  @Test
  fun loadsRegularCoverageReport(): Unit = runBlocking {
    val metadata = mapOf<String, ReportMetadata>(
      JVM_COVERAGE to coverageArtifact("icWithProblemReport", "JavaCoverageEngine.ic", JVM_COVERAGE),
    )
    highlightCoverageReport(metadata)

    val bundle = manager.currentSuitesBundle
    assertNotNull("Coverage suite was not opened", bundle)
    assertTrue("Unexpected engine: ${bundle!!.coverageEngine}", bundle.coverageEngine is JavaCoverageEngine)
    assertEquals(1, manager.activeSuites().size)

    // The tool window view is created and activated through QodanaCoverageToolWindowActivator.
    assertNotNull("Coverage view was not created", CoverageViewManager.getInstance(project).getView(bundle))

    // Full report: both covered files are shown in the tool window tree.
    assertCoverageTree(bundle, """
      -all
       -foo
        FooClass
        -bar
         UncoveredClass
         BarClass
    """.trimIndent())

    openFileInEditor("src/foo/FooClass.java")
    assertEquals(
      mapOf(5 to LineCoverage.FULL, 9 to LineCoverage.PARTIAL),
      gutterCoverage("src/foo/FooClass.java")
    )

    openFileInEditor("src/foo/bar/BarClass.java")
    assertEquals(
      mapOf(5 to LineCoverage.FULL, 9 to LineCoverage.NONE, 13 to LineCoverage.NONE),
      gutterCoverage("src/foo/bar/BarClass.java")
    )

    openFileInEditor("src/foo/bar/UncoveredClass.java")
    assertEquals(
      mapOf(5 to LineCoverage.NONE, 8 to LineCoverage.NONE, 11 to LineCoverage.NONE, 14 to LineCoverage.NONE),
      gutterCoverage("src/foo/bar/UncoveredClass.java")
    )
  }

  @Test
  fun loadsIncrementalCoverageReport(): Unit = runBlocking {
    val metadata = mapOf(
      JVM_COVERAGE to coverageArtifact("incrementalSecondStage", "JavaCoverageEngine.ic", JVM_COVERAGE),
      CHANGED_LINES_ARTIFACT_ID to changedLinesArtifact("incrementalSecondStage"),
    )
    highlightCoverageReport(metadata)

    val bundle = manager.currentSuitesBundle
    assertNotNull("Coverage suite was not opened", bundle)
    assertTrue("Unexpected engine: ${bundle!!.coverageEngine}", bundle.coverageEngine is JavaCoverageEngine)
    assertEquals(1, manager.activeSuites().size)

    // The tool window view is created and activated through QodanaCoverageToolWindowActivator.
    assertNotNull("Coverage view was not created", CoverageViewManager.getInstance(project).getView(bundle))

    assertCoverageTree(bundle, """
      -all
       -foo
        FooClass
    """.trimIndent())

    openFileInEditor("src/foo/FooClass.java")
    assertEquals(
      mapOf(5 to LineCoverage.FULL),
      gutterCoverage("src/foo/FooClass.java")
    )

    openFileInEditor("src/foo/bar/BarClass.java")
    assertTrue(
      "The src/foo/bar/BarClass.java should be uncovered",
      gutterCoverage("src/foo/bar/BarClass.java").isNullOrEmpty()
    )

    openFileInEditor("src/foo/bar/UncoveredClass.java")
    assertTrue(
      "The src/foo/bar/UncoveredClass.java should be uncovered",
      gutterCoverage("src/foo/bar/UncoveredClass.java").isNullOrEmpty()
    )

  }
}
