package org.jetbrains.qodana.cpp

import com.intellij.codeInspection.InspectionEP
import com.intellij.coverage.CoverageEngine
import com.intellij.coverage.CoverageFileProvider
import com.intellij.coverage.CoverageRunner
import com.intellij.ide.starter.extended.allure.Subsystems
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.DefaultPluginDescriptor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.rt.coverage.util.ProjectDataLoader
import com.intellij.testFramework.replaceService
import com.jetbrains.cidr.cpp.execution.coverage.CMakeCoverageEngine
import com.jetbrains.cidr.execution.coverage.CidrCoverageSuite
import com.jetbrains.cidr.execution.coverage.CidrCoverageSettings
import com.jetbrains.cidr.execution.coverage.gcov.GCovCoverageRunner
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.PRINTED_EXCEPTION_LIMIT
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageInspectionTest
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageLoadingListener
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.remapCoverageFromCloud
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import org.junit.Test
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Path

@Subsystems.Qodana
class CppCoverageInspectionTest : QodanaCoverageInspectionTest(INSPECTION_NAME) {
  private val dummyProvider = object : CoverageFileProvider {
    override fun getCoverageDataFilePath() = ""

    override fun ensureFileExists(): Boolean = true

    override fun isValid(): Boolean = true
  }

  override fun getProjectSourcesPath(): Path = testData.resolve("CppCoverageDiscoveryTest/project")

  override fun runInDispatchThread(): Boolean = false

  override fun setUpProject() {
    super.setUpProject()
    registerCoverageExtensions()
    ApplicationManager.getApplication().replaceService(
      CidrCoverageSettings::class.java,
      CidrCoverageSettings(),
      testRootDisposable,
    )
  }

  private fun registerCoverageExtensions() {
    val tool = CppCoverageInspection()
    val inspection = InspectionEP(
      tool.javaClass.canonicalName,
      DefaultPluginDescriptor(PluginId.getId("qodanaCppCoverageTest"), javaClass.classLoader),
    ).apply {
      shortName = INSPECTION_NAME
      displayName = "C++ coverage"
      groupDisplayName = "Coverage"
      level = "WARNING"
      enabledByDefault = true
      implementationClass = tool.javaClass.canonicalName
    }
    InspectionEP.GLOBAL_INSPECTION.point.registerExtension(inspection, testRootDisposable)
    CoverageEngine.EP_NAME.point.registerExtension(CMakeCoverageEngine(), testRootDisposable)
    CoverageRunner.EP_NAME.point.registerExtension(GCovCoverageRunner(), testRootDisposable)
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
  }

  @Test
  @Suppress("IO_FILE_USAGE")
  fun cpp() {
    runUnderCover()
    assertSarifResults()
    assertChangedLines(mapOf())
    assertCoverageProjectDataMatchesGolden(ENGINE_NAME, GOLDEN_FILE_NAME)

    val engine = CoverageEngine.EP_NAME.findExtensionOrFail(CMakeCoverageEngine::class.java)
    val path = qodanaConfig.coverage.coveragePath.resolve(ENGINE_NAME)
    val coverageRunner = CoverageRunner.getInstanceById(GCovCoverageRunner.ID)!!
    val data = ProjectDataLoader.load(path.toFile())
    val projectDir = qodanaConfig.projectPath
    assertFalse(data.classes.any { Path.of(it.key).startsWith(projectDir) })

    val suite = CidrCoverageSuite(engine, "test", project, coverageRunner, dummyProvider, -1)
    val coverageData = remapCoverageFromCloud(suite, data, emptyMap())?.coverageData
    assertNotNull(coverageData)
    assertSize(1, coverageData!!.classes.entries)
    assertTrue(coverageData.classes.all { Path.of(it.key).startsWith(projectDir) })
  }

  @Test
  fun coverageInfoWithProblemReport() {
    runUnderCover()
    assertSarifResults()
    assertCoverageProjectDataMatchesGolden(ENGINE_NAME, GOLDEN_FILE_NAME)
  }

  @Test
  fun coverageFromCustomLocation() {
    runUnderCoverDataInSources()
    assertSarifResults()
  }

  @Test
  fun coverageInfoWithoutProblemReport() {
    runUnderCover()
    assertSarifResults()
    assertCoverageProjectDataMatchesGolden(ENGINE_NAME, GOLDEN_FILE_NAME)
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
    runIncrementalAnalysis(QodanaCoverageComputationState.INCREMENTAL_REPORT, SCOPE)
    assertChangedLines(mapOf("main.cpp" to (1..6).toSet()))
    assertCoverageProjectDataMatchesGolden(ENGINE_NAME, GOLDEN_FILE_NAME)
    assertSarifResults()
  }

  @Test
  fun incrementalSecondStageWithoutProblemReport() {
    runIncrementalAnalysis(QodanaCoverageComputationState.INCREMENTAL_REPORT, SCOPE)
    assertFalse(qodanaConfig.coverage.reportProblems)
    assertNoCoverageProblems()
    assertSarifResults()
  }

  @Test
  fun invalidCoverageFileWithOneError() {
    val stdErr = runTestWithStderrCaptured {
      runUnderCover()
    }
    val expectedError = "The coverage data from $coverageFilePath may be loaded incorrectly because of: $INVALID_LINE_ERROR"
    assertEquals(1, expectedError.toRegex().findAll(stdErr).count())
  }

  @Test
  fun invalidCoverageFileWithMultipleErrors() {
    val stdErr = runTestWithStderrCaptured {
      runUnderCover()
    }
    val expectedError = "The coverage data from $coverageFilePath may be loaded incorrectly because of:"
    assertEquals(PRINTED_EXCEPTION_LIMIT, expectedError.toRegex().findAll(stdErr).count())

    val limitError = QodanaCoverageLoadingListener.buildTooManyErrorMessage(coverageFilePath)
    assertEquals(1, limitError.toRegex().findAll(stdErr).count())
  }

  private val coverageFilePath: String
    get() = getTestDataPath("coverage/coverage.gcov").toCanonicalPath()

  private fun runTestWithStderrCaptured(action: () -> Unit): String {
    val oldStdErr = System.err
    return ByteArrayOutputStream().use { stream ->
      try {
        BufferedOutputStream(stream).use { bufferedStream ->
          System.setErr(PrintStream(bufferedStream))
          action()
          bufferedStream.flush()
        }
      }
      finally {
        System.setErr(oldStdErr)
      }
      stream.toString()
    }
  }

  private companion object {
    private const val INSPECTION_NAME = "CppCoverageInspection"
    private const val ENGINE_NAME = "CMakeCoverageEngine"
    private const val GOLDEN_FILE_NAME = "CMakeCoverageEngine.gcov"
    private const val INVALID_LINE_ERROR = "Unrecognized line in .gcov file: invalid coverage line"

    private const val SCOPE = """
      {
        "files" : [ {
          "path" : "main.cpp",
          "added" : [ {
            "firstLine" : 1,
            "count" : 6
          } ],
          "deleted" : [ ]
        } ]
      }
    """
  }
}
