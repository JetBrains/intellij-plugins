package org.jetbrains.qodana.js

import com.intellij.coverage.CoverageEngine
import com.intellij.coverage.CoverageFileProvider
import com.intellij.coverage.CoverageRunner
import com.intellij.javascript.testing.coverage.jest.JestCoverageEngine
import com.intellij.rt.coverage.util.ProjectDataLoader
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageInspectionTest
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.remapCoverageFromCloud
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.junit.Test
import java.nio.file.Path

class JsCoverageInspectionTest: QodanaCoverageInspectionTest("JsCoverageInspection") {
  private val dummyProvider: CoverageFileProvider = object : CoverageFileProvider {
    override fun getCoverageDataFilePath() = ""

    override fun ensureFileExists(): Boolean = true

    override fun isValid(): Boolean = true
  }

  @Test
  fun jest() {
    runUnderCover()
    assertSarifResults()
    assertChangedLines(mapOf())
    assertCoverageProjectDataMatchesGolden("JestCoverageEngine", "JestCoverageEngine.info")

    val engine = CoverageEngine.EP_NAME.findExtensionOrFail(JestCoverageEngine::class.java)
    val path = qodanaConfig.coverage.coveragePath.resolve("JestCoverageEngine")
    val coverageRunner = CoverageRunner.getInstanceById(JestCoverageEngine.ID)!!
    val data = ProjectDataLoader.load(path.toFile())
    val projectDir = qodanaConfig.projectPath
    // assert that prefix was removed before storing the data
    assertFalse(data.classes.any { Path.of(it.key).startsWith(projectDir) })
    val suite = engine.createCoverageSuite("test", project, coverageRunner, dummyProvider, -1)!!
    val bundle = remapCoverageFromCloud(suite, data, emptyMap())
    val coverageData = bundle?.coverageData
    assertNotNull(coverageData)
    assertSize(2, coverageData!!.classes.entries)
    // assert that prefix was restored and bundle was correctly built
    assertTrue(coverageData.classes.all { Path.of(it.key).startsWith(projectDir) })
  }

  @Test
  fun jsWithoutProblemReport() {
    runUnderCover()
    assertSarifResults()
    assertCoverageProjectDataMatchesGolden("JestCoverageEngine", "JestCoverageEngine.info")
  }

  @Test
  fun jsWithProblemReport() {
    runUnderCover()
    assertSarifResults()
    assertCoverageProjectDataMatchesGolden("JestCoverageEngine", "JestCoverageEngine.info")
  }

  @Test
  fun coverageFromCustomLocation() {
    runUnderCoverDataInSources()
    assertSarifResults()
  }

  @Test
  fun warnMissingCoverage() {
    runUnderCover("inspection-profile.xml")
    assertSarifResults()
  }

  @Test
  fun incrementalFirstStage() {
    runIncrementalAnalysis(QodanaCoverageComputationState.SKIP_COMPUTE, SCOPE)
    assertSarifResults()
  }

  @Test
  fun incrementalSecondStage() {
    runIncrementalAnalysis(QodanaCoverageComputationState.SKIP_REPORT, SCOPE)
    assertCoverageProjectDataMatchesGolden("JestCoverageEngine", "JestCoverageEngine.info")
    assertChangedLinesMatchesGolden()
    assertSarifResults()
  }

  private companion object {
    // bar() of FooCls.ts: lines 19-21 are covered (DA counts > 0 in lcov.info), so fresh coverage is non-zero.
    private const val SCOPE = """
      {
        "files" : [ {
          "path" : "FooCls.ts",
          "added" : [ {
            "firstLine" : 19,
            "count" : 3
          } ],
          "deleted" : [ ]
        } ]
      }
    """
  }
}