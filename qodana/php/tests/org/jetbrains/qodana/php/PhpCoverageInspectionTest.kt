package org.jetbrains.qodana.php

import com.intellij.coverage.CoverageEngine
import com.intellij.coverage.CoverageFileProvider
import com.intellij.coverage.CoverageRunner
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.idea.IJIgnore
import com.intellij.rt.coverage.util.ProjectDataLoader
import com.intellij.testFramework.UsefulTestCase
import com.jetbrains.php.phpunit.coverage.PhpUnitCoverageEngine
import junit.framework.TestCase
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageInspectionTest
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.remapCoverageFromCloud
import org.junit.Test
import java.nio.file.Path

@IJIgnore(issue = "QD-10938")
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

    val engine = CoverageEngine.EP_NAME.findExtensionOrFail(PhpUnitCoverageEngine::class.java)
    val path = qodanaConfig.coverage.coveragePath.resolve("PhpUnitCoverageEngine")
    val coverageRunner = CoverageRunner.getInstanceById("PhpCoverage")!!
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
  fun coverageInfoWithProblemReport() {
    runUnderCoverDataInSources()
    assertSarifResults()
  }

  @Test
  fun coverageInfoWithoutProblemReport() {
    runUnderCoverDataInSources()
    assertSarifResults()
  }

  @Test
  fun warnMissingCoverage() {
    runUnderCover("inspection-profile.xml")
    assertSarifResults()
  }
}