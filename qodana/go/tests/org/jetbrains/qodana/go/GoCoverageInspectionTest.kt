package org.jetbrains.qodana.go

import com.goide.execution.testing.coverage.GoCoverageEngine
import com.goide.sdk.GoSdk
import com.goide.sdk.GoSdkService
import com.goide.sdk.GoSdkVersion
import com.intellij.coverage.CoverageEngine
import com.intellij.coverage.CoverageFileProvider
import com.intellij.coverage.CoverageRunner
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.rt.coverage.util.ProjectDataLoader
import com.intellij.testFramework.UsefulTestCase
import junit.framework.TestCase
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.PRINTED_EXCEPTION_LIMIT
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageInspectionTest
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageLoadingListener
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.remapCoverageFromCloud
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.junit.Test
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.Icon


class GoCoverageInspectionTest: QodanaCoverageInspectionTest("GoCoverageInspection") {
  private val dummyProvider: CoverageFileProvider = object : CoverageFileProvider {
    override fun getCoverageDataFilePath() = ""

    override fun ensureFileExists(): Boolean = true

    override fun isValid(): Boolean = true
  }

  override fun setUpProject() {
    super.setUpProject()
    GoSdkService.getInstance(project).setSdk(object : GoSdk {
      override fun getIcon(): Icon? = null

      override fun getVersion(): String?  = null

      override fun getHomeUrl(): String = project.basePath!!

      override fun getSrcDir(): VirtualFile? = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(Path.of(project.basePath!!).parent)

      override fun getExecutable(): VirtualFile? = null

      override fun isValid(): Boolean = false

      override fun getMajorVersion(): GoSdkVersion = GoSdkVersion.GO_1_1

      override fun getVersionFilePath(): String? = null

      override fun getSdkRoot(): VirtualFile? = null

      override fun getRootsToAttach(): MutableCollection<VirtualFile> = mutableListOf()

      override fun toString(): String = "Test Go SDK"
    }, false)
  }

  @Test
  fun go() {
    runUnderCover()
    assertSarifResults()
    assertCoverageProjectDataMatchesGolden("GoCoverageEngine", "GoCoverageEngine.out")
    assertGoCoverageProjectDataMatchesGolden()

    val engine = CoverageEngine.EP_NAME.findExtensionOrFail(GoCoverageEngine::class.java)
    val path = qodanaConfig.coverage.coveragePath.resolve("GoCoverageEngine")
    val coverageRunner = CoverageRunner.getInstanceById("GoCoverage")!!
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

  /**
   * Assert that the produced Go project-data sidecar matches the golden stored in `artifacts/<goldenFileName>`,
   * creating the golden from the produced artifact when it is missing. Mirrors [assertChangedLinesMatchesGolden];
   * the comparison is order-insensitive because the ranges originate from a hash map whose iteration order is not
   * stable. The same golden doubles as the fixture consumed by [GoCoverageUiTest].
   */
  private fun assertGoCoverageProjectDataMatchesGolden(goldenFileName: String = GO_PROJECT_DATA_FILE_NAME) {
    val actualFile = qodanaConfig.coverage.coveragePath.resolve(GO_PROJECT_DATA_FILE_NAME)
    assertTrue("Go project-data sidecar was not produced at $actualFile", Files.isRegularFile(actualFile))

    val goldenFile = getTestDataPath("artifacts/$goldenFileName")
    if (!Files.isRegularFile(goldenFile)) {
      Files.createDirectories(goldenFile.parent)
      Files.copy(actualFile, goldenFile)
      fail("Golden Go project-data artifact was missing and has been created at $goldenFile. Re-run the test to validate it.")
    }

    val expected = readGoCoverageProjectData(goldenFile)
    val actual = readGoCoverageProjectData(actualFile)
    assertEquals(expected.mapValues { it.value.toSet() }, actual.mapValues { it.value.toSet() })
  }

  @Test
  fun coverageInfoWithProblemReport() {
    runUnderCover()
    assertSarifResults()
    assertCoverageProjectDataMatchesGolden("GoCoverageEngine", "GoCoverageEngine.out")
  }

  @Test
  fun coverageInfoWithoutProblemReport() {
    runUnderCover()
    assertSarifResults()
    assertCoverageProjectDataMatchesGolden("GoCoverageEngine", "GoCoverageEngine.out")
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
    assertChangedLines(mapOf("coverage.go" to setOf(3, 4, 5)))
    assertCoverageProjectDataMatchesGolden("GoCoverageEngine", "GoCoverageEngine.out")
    assertGoCoverageProjectDataMatchesGolden()
    assertSarifResults()
  }

  private val coverageFilePath
    get() = Paths.get(project.basePath!!, "..", testDirectoryName, "coverage", "coverage.out").toCanonicalPath()

  private val expectedError
    get() = """
      The coverage data from $coverageFilePath may be loaded incorrectly because of: Parsing error in line: sources/coverage.go:8.13,10.3 X X
      java.lang.NumberFormatException: For input string: "X"
    """.trimIndent()

  private val exceptionMessage = "For input string: \"X\""

  @Test
  fun invalidCoverageFileWithOneError() {
    val stdErr = runTestWithStderrCaptured {
      runUnderCover()
    }
    // checks that both message and exception were collected
    assertTrue(stdErr.contains(expectedError))
    // checks that only one exception was printed to stderr
    assertTrue(exceptionMessage.toRegex().findAll(stdErr).count() == 1)
  }

  private val expectedErrorMessageRegex
    get() = "The coverage data from $coverageFilePath may be loaded incorrectly because of:".toRegex()

  @Test
  fun invalidCoverageFileWithMultipleErrors() {
    val stdErr = runTestWithStderrCaptured {
      runUnderCover()
    }
    // checks that exactly PRINTED_EXCEPTION_LIMIT errors were printed to stderr
    assertTrue(expectedErrorMessageRegex.findAll(stdErr).count() == PRINTED_EXCEPTION_LIMIT)

    // checks that message about more exceptions was printed exactly once
    val expectedErrorMessage = QodanaCoverageLoadingListener.buildTooManyErrorMessage(coverageFilePath)
    assertTrue(expectedErrorMessage.toRegex().findAll(stdErr).count() == 1)
  }

  private fun runTestWithStderrCaptured(runnable: () -> Unit): String {
    val oldStdErr = System.err
    return ByteArrayOutputStream().use { stream ->
      try {
        BufferedOutputStream(stream).use { bufferedStream ->
          System.setErr(PrintStream(bufferedStream))
          runnable()
          bufferedStream.flush()
        }
      } finally {
        System.setErr(oldStdErr)
      }
      stream.toString()
    }
  }

  private companion object {
    // Foo() spans lines 3-5; line 4 (`return x + y`) is covered, so fresh coverage is non-zero.
    private const val SCOPE = """
      {
        "files" : [ {
          "path" : "coverage.go",
          "added" : [ {
            "firstLine" : 3,
            "count" : 3
          } ],
          "deleted" : [ ]
        } ]
      }
    """
  }
}