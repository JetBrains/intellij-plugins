package org.jetbrains.qodana.js

import com.intellij.coverage.CoverageEngine
import com.intellij.coverage.CoverageFileProvider
import com.intellij.coverage.CoverageRunner
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.javascript.jest.coverage.JestCoverageEngine
import com.intellij.rt.coverage.util.ProjectDataLoader
import com.intellij.testFramework.UsefulTestCase
import junit.framework.TestCase
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageInspectionTest
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.remapCoverageFromCloud
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

    val engine = CoverageEngine.EP_NAME.findExtensionOrFail(JestCoverageEngine::class.java)
    val path = qodanaConfig.coverage.coveragePath.resolve("JestCoverageEngine")
    val coverageRunner = CoverageRunner.getInstanceById(JestCoverageEngine.ID)!!
    val data = ProjectDataLoader.load(path.toFile())
    val projectDir = qodanaConfig.projectPath
    // assert that prefix was removed before storing the data
    assertFalse(data.classes.any { Path.of(it.key).startsWith(projectDir) })
    val suite = engine.createCoverageSuite("test", project, coverageRunner, dummyProvider, -1)!!
    suite.setCoverageData(data)
    val bundle = remapCoverageFromCloud(CoverageSuitesBundle(suite))
    val coverageData = bundle?.coverageData
    TestCase.assertNotNull(coverageData)
    UsefulTestCase.assertSize(2, coverageData!!.classes.entries)
    // assert that prefix was restored and bundle was correctly built
    assertTrue(coverageData.classes.all { Path.of(it.key).startsWith(projectDir) })
  }

  @Test
  fun jsWithoutProblemReport() {
    runUnderCover()
    assertSarifResults()
  }

  @Test
  fun jsWithProblemReport() {
    runUnderCover()
    assertSarifResults()
  }

  @Test
  fun warnMissingCoverage() {
    runUnderCover("inspection-profile.xml")
    assertSarifResults()
  }
}