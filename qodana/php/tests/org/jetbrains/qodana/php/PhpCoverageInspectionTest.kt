package org.jetbrains.qodana.php

import com.intellij.coverage.CoverageEngine
import com.intellij.coverage.CoverageFileProvider
import com.intellij.coverage.CoverageRunner
import com.intellij.php.coverage.PhpUnitCoverageEngine
import com.intellij.rt.coverage.util.ProjectDataLoader
import com.intellij.testFramework.UsefulTestCase
import junit.framework.TestCase
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageInspectionTest
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.remapCoverageFromCloud
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.junit.Test
import java.nio.file.Path

class PhpCoverageInspectionTest: QodanaCoverageInspectionTest("PhpCoverageInspection") {
  private val dummyProvider: CoverageFileProvider = object : CoverageFileProvider {
    override fun getCoverageDataFilePath() = ""

    override fun ensureFileExists(): Boolean = true

    override fun isValid(): Boolean = true
  }

  @Test
  fun phpunit() {
    runUnderCover()
    assertSarifResults()
    assertCoverageProjectDataMatchesGolden("PhpUnitCoverageEngine", "PhpUnitCoverageEngine.xml")

    val engine = CoverageEngine.EP_NAME.findExtensionOrFail(PhpUnitCoverageEngine::class.java)
    val path = qodanaConfig.coverage.coveragePath.resolve("PhpUnitCoverageEngine")
    val coverageRunner = CoverageRunner.getInstanceById("PhpCoverage")!!
    val data = ProjectDataLoader.load(path.toFile())
    val projectDir = qodanaConfig.projectPath
    // assert that prefix was removed before storing the data
    assertFalse(data.classes.any { Path.of(it.key).startsWith(projectDir) })
    val suite = engine.createCoverageSuite("test", project, coverageRunner, dummyProvider, -1)!!
    val bundle = remapCoverageFromCloud(suite, data, emptyMap())
    val coverageData = bundle?.coverageData
    TestCase.assertNotNull(coverageData)
    UsefulTestCase.assertSize(2, coverageData!!.classes.entries)
    // assert that prefix was restored and bundle was correctly built
    assertTrue(coverageData.classes.all { Path.of(it.key).startsWith(projectDir) })
  }

  @Test
  fun coverageInfoWithProblemReport() {
    runUnderCoverDataInSources()
    assertSarifResults()
    assertCoverageProjectDataMatchesGolden("PhpUnitCoverageEngine", "PhpUnitCoverageEngine.xml")
  }

  @Test
  fun coverageInfoWithoutProblemReport() {
    runUnderCoverDataInSources()
    assertSarifResults()
    assertCoverageProjectDataMatchesGolden("PhpUnitCoverageEngine", "PhpUnitCoverageEngine.xml")
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
    assertChangedLines(mapOf("src/FooCls.php" to setOf(14, 15, 16)))
    assertCoverageProjectDataMatchesGolden("PhpUnitCoverageEngine", "PhpUnitCoverageEngine.xml")
    assertSarifResults()
  }

  private companion object {
    // bar() of FooCls: lines 14-16 are covered (count=1 in coverage.xml), so fresh coverage is non-zero.
    private const val SCOPE = """
      {
        "files" : [ {
          "path" : "src/FooCls.php",
          "added" : [ {
            "firstLine" : 14,
            "count" : 3
          } ],
          "deleted" : [ ]
        } ]
      }
    """
  }
}