package org.jetbrains.qodana.python.coverage

import com.intellij.coverage.CoverageEngine
import com.intellij.coverage.CoverageFileProvider
import com.intellij.coverage.CoverageRunner
import com.intellij.python.pro.coverage.PyCoverageEngine
import com.intellij.rt.coverage.util.ProjectDataLoader
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageInspectionTest
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.remapCoverageFromCloud
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.junit.Test
import java.nio.file.Path

class PyCoverageInspectionTest : QodanaCoverageInspectionTest("PyCoverageInspection") {
  private val dummyProvider: CoverageFileProvider = object : CoverageFileProvider {
    override fun getCoverageDataFilePath() = ""

    override fun ensureFileExists(): Boolean = true

    override fun isValid(): Boolean = true
  }

  @Test
  fun py() {
    runUnderCover()
    assertSarifResults()
    assertCoverageProjectDataMatchesGolden("PyCoverageEngine", "PyCoverageEngine.xml")

    val engine = CoverageEngine.EP_NAME.findExtensionOrFail(PyCoverageEngine::class.java)
    val path = qodanaConfig.coverage.coveragePath.resolve("PyCoverageEngine")
    val coverageRunner = CoverageRunner.getInstanceById("coverage.py")!!
    val data = ProjectDataLoader.load(path.toFile())
    val projectDir = qodanaConfig.projectPath
    // assert that prefix was removed before storing the data
    assertFalse(data.classes.any { Path.of(it.key).startsWith(projectDir) })
    val suite = engine.createCoverageSuite("test", project, coverageRunner, dummyProvider, -1)
    val bundle = remapCoverageFromCloud(suite, data, emptyMap())
    val coverageData = bundle?.coverageData
    assertNotNull(coverageData)
    assertSize(3, coverageData!!.classes.entries)
    // assert that prefix was restored and bundle was correctly built
    assertTrue(coverageData.classes.all { Path.of(it.key).startsWith(projectDir) })
  }

  @Test
  fun coverageInfoWithoutProblemReport() {
    runUnderCover()
    assertSarifResults()
    assertCoverageProjectDataMatchesGolden("PyCoverageEngine", "PyCoverageEngine.xml")
  }

  @Test
  fun coverageInfoWithProblemReport() {
    runUnderCover()
    assertSarifResults()
    assertCoverageProjectDataMatchesGolden("PyCoverageEngine", "PyCoverageEngine.xml")
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
    assertChangedLines(mapOf("src/FooCls.py" to setOf(11, 12, 13)))
    assertCoverageProjectDataMatchesGolden("PyCoverageEngine", "PyCoverageEngine.xml")
    assertSarifResults()
  }

  private companion object {
    // bar() of FooCls.py: lines 11-13 are covered (hits > 0 in coverage.xml), so fresh coverage is non-zero.
    private const val SCOPE = """
      {
        "files" : [ {
          "path" : "src/FooCls.py",
          "added" : [ {
            "firstLine" : 11,
            "count" : 3
          } ],
          "deleted" : [ ]
        } ]
      }
    """
  }
}