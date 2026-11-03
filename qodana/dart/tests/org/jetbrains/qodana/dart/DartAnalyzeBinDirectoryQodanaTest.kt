package org.jetbrains.qodana.dart

import com.intellij.ide.analysisignore.AnalysisIgnoreDefaults
import com.intellij.ide.analysisignore.AnalysisIgnoreDefaultsSettings
import com.intellij.ide.analysisignore.AnalysisIgnoreService
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.workspaceModel.ide.registerProjectRootBlocking
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

private const val AUTO_RECONFIGURE_ANALYSISIGNORE_PROPERTY = "qodana.dart.autoreconfigure.analysisignore"

internal class DartAnalyzeBinDirectoryQodanaTest : DartAnalyzeProjectQodanaTestCase() {
  private val includeDartBinDirectory = QodanaIncludeDartBinDirectory()

  override val testDataBasePath: Path = Path.of(javaClass.simpleName)

  override val projectSourcePath: Path
    get() = testData.resolve("dart-analyze/bin-package")

  override val analyzeResponses: Map<String, RecordedDartAnalyzeResponse>
    get() = mapOf(
      "" to readDartAnalyzeTestData("bin-package.json"),
      "lib" to readDartAnalyzeTestData("bin-package-lib.json"),
    )

  override fun setUp() {
    super.setUp()
    AnalysisIgnoreDefaultsSettings.getInstance().setLines(AnalysisIgnoreDefaults.BUILT_IN_LINES)
    registerProjectRootBlocking(project, qodanaConfig.projectPath)
  }

  private fun beforeProjectOpened() {
    runBlocking { includeDartBinDirectory.beforeProjectOpened(qodanaConfig) }
    AnalysisIgnoreService.getInstance(project).syncDefaultsBlocking()
    IndexingTestUtil.waitUntilIndexesAreReady(project)
  }

  override fun tearDown() {
    AnalysisIgnoreDefaultsSettings.getInstance().setLines(AnalysisIgnoreDefaults.BUILT_IN_LINES)
    super.tearDown()
  }

  @Test
  fun `reports a Dart diagnostic from the bin directory of a Dart package`() {
    assertEquals(0, openProjectAndCountBinWarnings())
    runAnalysis()

    assertEquals(listOf("bin/main.dart", "lib/lib.dart"), sarifResultPaths())
  }

  @Test
  fun `does not reconfigure default exclusions when the feature is disabled`() {
    PlatformTestUtil.withSystemProperty<RuntimeException>(AUTO_RECONFIGURE_ANALYSISIGNORE_PROPERTY, "false") {
      assertEquals(0, openProjectAndCountBinWarnings())
    }

    assertEquals(AnalysisIgnoreDefaults.BUILT_IN_LINES, AnalysisIgnoreDefaultsSettings.getInstance().lines)
    runAnalysis()

    assertEquals(listOf("lib/lib.dart"), sarifResultPaths())
  }

  @Test
  fun `does not reconfigure customized exclusions when the feature is disabled`() {
    val customLines = AnalysisIgnoreDefaults.BUILT_IN_LINES + "/custom/"
    AnalysisIgnoreDefaultsSettings.getInstance().setLines(customLines)

    PlatformTestUtil.withSystemProperty<RuntimeException>(AUTO_RECONFIGURE_ANALYSISIGNORE_PROPERTY, "false") {
      assertEquals(0, openProjectAndCountBinWarnings())
    }

    assertEquals(customLines, AnalysisIgnoreDefaultsSettings.getInstance().lines)
    runAnalysis()

    assertEquals(listOf("lib/lib.dart"), sarifResultPaths())
  }

  @Test
  fun `keeps the default exclusions of a project without pubspec yaml`() {
    Files.delete(qodanaConfig.projectPath.resolve("pubspec.yaml"))
    assertEquals(0, openProjectAndCountBinWarnings())

    assertEquals(AnalysisIgnoreDefaults.BUILT_IN_LINES, AnalysisIgnoreDefaultsSettings.getInstance().lines)
  }

  @Test
  fun `keeps the customized exclusions without a warning for a project without pubspec yaml`() {
    Files.delete(qodanaConfig.projectPath.resolve("pubspec.yaml"))
    val customLines = AnalysisIgnoreDefaults.BUILT_IN_LINES + "/custom/"
    AnalysisIgnoreDefaultsSettings.getInstance().setLines(customLines)

    assertEquals(0, openProjectAndCountBinWarnings())
    assertEquals(customLines, AnalysisIgnoreDefaultsSettings.getInstance().lines)
  }

  @Test
  fun `keeps the exclusions that the user customized`() {
    val customLines = AnalysisIgnoreDefaults.BUILT_IN_LINES + "/custom/"
    AnalysisIgnoreDefaultsSettings.getInstance().setLines(customLines)
    assertEquals(1, openProjectAndCountBinWarnings())

    assertEquals(customLines, AnalysisIgnoreDefaultsSettings.getInstance().lines)
    runAnalysis()

    assertEquals(listOf("lib/lib.dart"), sarifResultPaths())
  }

  @Test
  fun `includes the bin directory when the customized exclusions do not exclude it`() {
    val customLines = (AnalysisIgnoreDefaults.BUILT_IN_LINES - "/bin/") + "/custom/"
    AnalysisIgnoreDefaultsSettings.getInstance().setLines(customLines)
    assertEquals(0, openProjectAndCountBinWarnings())

    assertEquals(customLines, AnalysisIgnoreDefaultsSettings.getInstance().lines)
    runAnalysis()

    assertEquals(listOf("bin/main.dart", "lib/lib.dart"), sarifResultPaths())
  }

  private fun openProjectAndCountBinWarnings(): Int {
    val output = ByteArrayOutputStream()
    val standardOut = System.out
    PrintStream(output, true, Charsets.UTF_8).use { stream ->
      try {
        System.setOut(stream)
        beforeProjectOpened()
      }
      finally {
        System.setOut(standardOut)
      }
    }
    val warning = "WARN - ${QodanaDartBundle.message("dart.analyze.warning.bin.excluded", qodanaConfig.projectPath)}"
    return output.toString(Charsets.UTF_8).lineSequence().count { it == warning }
  }
}