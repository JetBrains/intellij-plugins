package org.jetbrains.qodana.go

import com.goide.execution.testing.coverage.GoCoverageEngine
import com.goide.execution.testing.coverage.GoCoverageProjectData
import com.intellij.coverage.view.CoverageViewManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.rt.coverage.data.LineCoverage
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.coverage.CHANGED_LINES_ARTIFACT_ID
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageUiTestBase
import org.junit.Test
import kotlin.io.path.absolute
import kotlin.io.path.pathString

private const val SOURCE_CLASS = "GoCoverageInspectionTest"

class GoCoverageUiTest : QodanaCoverageUiTestBase(SOURCE_CLASS) {

  @Test
  fun loadsRegularCoverageReport(): Unit = runBlocking {
    val metadata = mapOf(
      GO_COVERAGE to coverageArtifact("go", "GoCoverageEngine.out", GO_COVERAGE),
      GO_PROJECT_DATA to goProjectDataArtifact("go"),
    )
    highlightCoverageReport(metadata)

    val bundle = manager.currentSuitesBundle
    assertNotNull("Coverage suite was not opened", bundle)
    assertTrue("Unexpected engine: ${bundle!!.coverageEngine}", bundle.coverageEngine is GoCoverageEngine)
    assertEquals(1, manager.activeSuites().size)
    assertNotNull("Coverage view was not created", CoverageViewManager.getInstance(project).getView(bundle))

    assertCoverageTree(bundle, """
      -sources
       coverage.go
       not_covered.go
    """.trimIndent())

    openFileInEditor("coverage.go")
    assertEquals(
      mapOf(
        3 to LineCoverage.FULL, 4 to LineCoverage.FULL, 5 to LineCoverage.FULL,
        7 to LineCoverage.FULL, 8 to LineCoverage.FULL, 9 to LineCoverage.FULL, 10 to LineCoverage.FULL,
        11 to LineCoverage.NONE, 12 to LineCoverage.NONE, 13 to LineCoverage.NONE,
        16 to LineCoverage.NONE, 17 to LineCoverage.NONE, 18 to LineCoverage.NONE, 19 to LineCoverage.NONE, 20 to LineCoverage.NONE,
        22 to LineCoverage.FULL, 23 to LineCoverage.FULL, 24 to LineCoverage.FULL, 25 to LineCoverage.FULL, 26 to LineCoverage.FULL,
        27 to LineCoverage.FULL, 28 to LineCoverage.FULL, 29 to LineCoverage.FULL,
        30 to LineCoverage.FULL, 31 to LineCoverage.FULL,
        34 to LineCoverage.FULL,
        35 to LineCoverage.PARTIAL,
        36 to LineCoverage.NONE, 37 to LineCoverage.NONE, 38 to LineCoverage.NONE, 39 to LineCoverage.NONE,
        40 to LineCoverage.PARTIAL,
        41 to LineCoverage.NONE, 42 to LineCoverage.NONE, 43 to LineCoverage.NONE,
        44 to LineCoverage.PARTIAL,
        45 to LineCoverage.NONE, 46 to LineCoverage.NONE,
        47 to LineCoverage.FULL,
        57 to LineCoverage.FULL, 58 to LineCoverage.FULL, 59 to LineCoverage.FULL,
        61 to LineCoverage.NONE, 62 to LineCoverage.NONE, 63 to LineCoverage.NONE, 64 to LineCoverage.NONE, 65 to LineCoverage.NONE,
        67 to LineCoverage.FULL, 68 to LineCoverage.FULL, 69 to LineCoverage.FULL, 70 to LineCoverage.FULL, 71 to LineCoverage.FULL, 72 to LineCoverage.FULL,
        74 to LineCoverage.NONE, 75 to LineCoverage.NONE, 76 to LineCoverage.NONE, 77 to LineCoverage.NONE,
        78 to LineCoverage.NONE, 79 to LineCoverage.NONE, 80 to LineCoverage.NONE, 81 to LineCoverage.NONE, 82 to LineCoverage.NONE, 83 to LineCoverage.NONE,
      ),
      gutterCoverage("coverage.go"),
    )
    openFileInEditor("not_covered.go")
    assertEquals(
      mapOf(
        3 to LineCoverage.FULL, 4 to LineCoverage.FULL, 5 to LineCoverage.FULL,
        7 to LineCoverage.NONE, 8 to LineCoverage.NONE, 9 to LineCoverage.NONE,
        11 to LineCoverage.NONE, 12 to LineCoverage.NONE, 13 to LineCoverage.NONE,
      ),
      gutterCoverage("not_covered.go"),
    )

    openFileInEditor("coverage_test.go")
    assertTrue(
      "coverage_test.go should not be highlighted",
      gutterCoverage("coverage_test.go").isNullOrEmpty()
    )

    openFileInEditor("not_reported.go")
    assertTrue(
      "not_reported.go should not be highlighted",
      gutterCoverage("not_reported.go").isNullOrEmpty()
    )
  }

  @Test
  fun loadsIncrementalCoverageReport(): Unit = runBlocking {
    val metadata = mapOf(
      GO_COVERAGE to coverageArtifact("incrementalSecondStage", "GoCoverageEngine.out", GO_COVERAGE),
      GO_PROJECT_DATA to goProjectDataArtifact("incrementalSecondStage"),
      CHANGED_LINES_ARTIFACT_ID to changedLinesArtifact("incrementalSecondStage"),
    )
    highlightCoverageReport(metadata)

    val bundle = manager.currentSuitesBundle
    assertNotNull("Coverage suite was not opened", bundle)
    assertNotNull("Coverage view was not created", CoverageViewManager.getInstance(project).getView(bundle!!))

    assertCoverageTree(bundle, """
      -sources
       coverage.go
    """.trimIndent())

    openFileInEditor("coverage.go")
    assertEquals(
      mapOf(3 to LineCoverage.FULL, 4 to LineCoverage.FULL, 5 to LineCoverage.FULL),
      gutterCoverage("coverage.go"),
    )

    openFileInEditor("not_covered.go")
    assertTrue(
      "not_covered.go should not be highlighted in incremental report",
      gutterCoverage("not_covered.go").isNullOrEmpty()
    )

    openFileInEditor("coverage_test.go")
    assertTrue(
      "coverage_test.go should not be highlighted",
      gutterCoverage("coverage_test.go").isNullOrEmpty()
    )

    openFileInEditor("not_reported.go")
    assertTrue(
      "not_reported.go should not be highlighted",
      gutterCoverage("not_reported.go").isNullOrEmpty()
    )

    (bundle.coverageData as? GoCoverageProjectData)?.apply {
      val fileToHits = mutableMapOf<String, Long>()
      processFiles { fileData ->
        fileData.myRangesData.forEach { rangeData ->
          val newValue = fileToHits.getOrPut(fileData.myFilePath) { 0L } + rangeData.value.hits
          fileToHits[fileData.myFilePath] = newValue
        }
        true
      }
      val coveredFile = project.guessProjectDir()?.findFileByRelativePath("coverage.go")?.toNioPath()?.absolute()?.pathString
      assertNotNull(coveredFile)
      assertTrue("Only three hits overall should in coverage.go stay after filtering", fileToHits[coveredFile!!] == 3L)
      assertTrue("Only one file should contain hits", fileToHits.size == 1)
    }
  }

  private fun goProjectDataArtifact(scenario: String): GoCoverageProjectDataArtifact {
    val golden = testData
      .resolve(SOURCE_CLASS)
      .resolve(scenario)
      .resolve("artifacts")
      .resolve(GO_PROJECT_DATA_FILE_NAME)
    return GoCoverageProjectDataArtifact(GO_PROJECT_DATA, golden)
  }
}
