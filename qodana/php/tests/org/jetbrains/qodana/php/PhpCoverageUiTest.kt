package org.jetbrains.qodana.php

import com.intellij.coverage.view.CoverageViewManager
import com.intellij.php.coverage.PhpUnitCoverageEngine
import com.intellij.rt.coverage.data.LineCoverage
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.coverage.CHANGED_LINES_ARTIFACT_ID
import org.jetbrains.qodana.report.ReportMetadata
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageUiTestBase
import org.junit.Test

class PhpCoverageUiTest : QodanaCoverageUiTestBase("PhpCoverageInspectionTest") {

  @Test
  fun loadsRegularCoverageReport(): Unit = runBlocking {
    val metadata = mapOf<String, ReportMetadata>(
      PHPUNIT_COVERAGE to coverageArtifact("phpunit", "PhpUnitCoverageEngine.xml", PHPUNIT_COVERAGE),
    )
    highlightCoverageReport(metadata)

    val bundle = manager.currentSuitesBundle
    assertNotNull("Coverage suite was not opened", bundle)
    assertTrue("Unexpected engine: ${bundle!!.coverageEngine}", bundle.coverageEngine is PhpUnitCoverageEngine)
    assertEquals(1, manager.activeSuites().size)
    assertNotNull("Coverage view was not created", CoverageViewManager.getInstance(project).getView(bundle))

    // index.php is not present here due to PHP check that files with zero coverage shoould be ignored
    // see com.intellij.php.coverage.PhpCoverageAnnotator.getLinesCoverageInformationString
    assertCoverageTree(bundle, """
      -sources
       -src
        FooCls.php
    """.trimIndent())

    openFileInEditor("src/FooCls.php")
    assertEquals(
      mapOf(
        7 to LineCoverage.FULL, 9 to LineCoverage.FULL,
        12 to LineCoverage.FULL,
        14 to LineCoverage.FULL, 15 to LineCoverage.FULL, 16 to LineCoverage.FULL, 18 to LineCoverage.NONE,
        21 to LineCoverage.FULL, 23 to LineCoverage.FULL, 24 to LineCoverage.FULL,
        26 to LineCoverage.NONE, 27 to LineCoverage.NONE, 28 to LineCoverage.NONE,
        31 to LineCoverage.NONE, 33 to LineCoverage.NONE, 39 to LineCoverage.NONE,
        44 to LineCoverage.FULL, 47 to LineCoverage.FULL,
        50 to LineCoverage.NONE, 51 to LineCoverage.NONE, 52 to LineCoverage.NONE,
        54 to LineCoverage.NONE, 55 to LineCoverage.NONE, 56 to LineCoverage.NONE,
        58 to LineCoverage.NONE, 59 to LineCoverage.NONE, 60 to LineCoverage.NONE,
        62 to LineCoverage.FULL, 64 to LineCoverage.FULL,
        66 to LineCoverage.NONE, 67 to LineCoverage.NONE, 68 to LineCoverage.FULL,
        71 to LineCoverage.NONE, 72 to LineCoverage.NONE,
      ),
      gutterCoverage("src/FooCls.php"),
    )
    openFileInEditor("src/index.php")
    assertEquals(
      mapOf(2 to LineCoverage.NONE),
      gutterCoverage("src/index.php"),
    )
  }

  @Test
  fun loadsIncrementalCoverageReport(): Unit = runBlocking {
    val metadata = mapOf(
      PHPUNIT_COVERAGE to coverageArtifact("incrementalSecondStage", "PhpUnitCoverageEngine.xml", PHPUNIT_COVERAGE),
      CHANGED_LINES_ARTIFACT_ID to changedLinesArtifact("incrementalSecondStage"),
    )
    highlightCoverageReport(metadata)

    val bundle = manager.currentSuitesBundle
    assertNotNull("Coverage suite was not opened", bundle)
    assertNotNull("Coverage view was not created", CoverageViewManager.getInstance(project).getView(bundle!!))

    assertCoverageTree(bundle, """
      -sources
       -src
        FooCls.php
    """.trimIndent())

    openFileInEditor("src/FooCls.php")
    assertEquals(
      mapOf(14 to LineCoverage.FULL, 15 to LineCoverage.FULL, 16 to LineCoverage.FULL),
      gutterCoverage("src/FooCls.php"),
    )
  }
}
