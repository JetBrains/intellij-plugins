package org.jetbrains.qodana.dart

import com.intellij.ide.analysisignore.AnalysisIgnoreDefaults
import com.intellij.ide.analysisignore.AnalysisIgnoreDefaultsSettings
import com.intellij.ide.analysisignore.AnalysisIgnoreService
import com.intellij.openapi.vfs.LocalFileSystem
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
  }

  private fun configureForQodana() {
    AnalysisIgnoreService.getInstance(project).syncDefaultsBlocking()
    IndexingTestUtil.waitUntilIndexesAreReady(project)
    runBlocking { includeDartBinDirectory.configureForQodana(qodanaConfig, project) }
  }

  private fun afterProjectClosed() {
    runBlocking { includeDartBinDirectory.afterProjectClosed(qodanaConfig) }
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
  fun `does not change exclusions before project configuration`() {
    val settings = AnalysisIgnoreDefaultsSettings.getInstance()

    beforeProjectOpened()

    assertEquals(AnalysisIgnoreDefaults.BUILT_IN_LINES, settings.lines)
    assertFalse(settings.isCustomized)
  }

  @Test
  fun `restores default exclusions after the project closes`() {
    val settings = AnalysisIgnoreDefaultsSettings.getInstance()
    val configuredLines = AnalysisIgnoreDefaults.BUILT_IN_LINES - "/bin/"

    assertEquals(0, openProjectAndCountBinWarnings())
    assertEquals(configuredLines, settings.lines)
    assertTrue(settings.isCustomized)
    assertEquals(configuredLines, settings.getState().lines)

    afterProjectClosed()

    assertEquals(AnalysisIgnoreDefaults.BUILT_IN_LINES, settings.lines)
    assertFalse(settings.isCustomized)
    assertTrue(settings.getState().lines.isEmpty())

    assertEquals(0, openProjectAndCountBinWarnings())
    assertEquals(configuredLines, settings.lines)
    afterProjectClosed()

    assertEquals(AnalysisIgnoreDefaults.BUILT_IN_LINES, settings.lines)
    assertFalse(settings.isCustomized)
    assertTrue(settings.getState().lines.isEmpty())
  }

  @Test
  fun `warns and keeps global exclusions when root analysisignore excludes bin`() {
    val settings = AnalysisIgnoreDefaultsSettings.getInstance()
    val content = "# Keep the bin directory excluded.\n/bin/\n"
    val analysisIgnore = writeAnalysisIgnore(content)

    assertEquals(1, openProjectAndCountBinWarnings())
    assertEquals(AnalysisIgnoreDefaults.BUILT_IN_LINES, settings.lines)
    assertFalse(settings.isCustomized)
    assertEquals(content, Files.readString(analysisIgnore))
    runAnalysis()

    assertEquals(listOf("lib/lib.dart"), sarifResultPaths())
    afterProjectClosed()

    assertEquals(AnalysisIgnoreDefaults.BUILT_IN_LINES, settings.lines)
    assertEquals(content, Files.readString(analysisIgnore))
  }

  @Test
  fun `warns when a wildcard in root analysisignore excludes bin`() {
    writeAnalysisIgnore("b*/\n")

    assertEquals(1, openProjectAndCountBinWarnings())
    assertEquals(AnalysisIgnoreDefaults.BUILT_IN_LINES, AnalysisIgnoreDefaultsSettings.getInstance().lines)
  }

  @Test
  fun `warns when customized default exclusions use a wildcard for bin`() {
    val settings = AnalysisIgnoreDefaultsSettings.getInstance()
    val customLines = (AnalysisIgnoreDefaults.BUILT_IN_LINES - "/bin/") + "b*/"
    settings.setLines(customLines)

    assertEquals(1, openProjectAndCountBinWarnings())
    afterProjectClosed()
    assertEquals(customLines, settings.lines)
  }

  @Test
  fun `does not warn when the root bin directory does not exist`() {
    val binDirectory = qodanaConfig.projectPath.resolve("bin")
    val backupDirectory = qodanaConfig.projectPath.resolve("bin-backup")
    assertFalse(Files.exists(backupDirectory))
    Files.move(binDirectory, backupDirectory)
    try {
      LocalFileSystem.getInstance().refreshAndFindFileByNioFile(binDirectory)
      assertEquals(0, openProjectAndCountBinWarnings())
      afterProjectClosed()
    }
    finally {
      Files.move(backupDirectory, binDirectory)
      LocalFileSystem.getInstance().refreshAndFindFileByNioFile(binDirectory)
    }
  }

  @Test
  fun `root analysisignore without supported bin exclusion overrides customized global exclusions`() {
    val settings = AnalysisIgnoreDefaultsSettings.getInstance()
    val customLines = AnalysisIgnoreDefaults.BUILT_IN_LINES + "/custom/"
    settings.setLines(customLines)
    val content = "# A comment does not exclude bin.\n!bin/\n"
    val analysisIgnore = writeAnalysisIgnore(content)

    assertEquals(0, openProjectAndCountBinWarnings())
    assertEquals(customLines, settings.lines)
    assertTrue(settings.isCustomized)
    assertEquals(content, Files.readString(analysisIgnore))
    runAnalysis()

    assertEquals(listOf("bin/main.dart", "lib/lib.dart"), sarifResultPaths())
    afterProjectClosed()

    assertEquals(customLines, settings.lines)
    assertEquals(content, Files.readString(analysisIgnore))
  }

  @Test
  fun `does not warn for root analysisignore when the feature is disabled`() {
    writeAnalysisIgnore("/bin/\n")

    PlatformTestUtil.withSystemProperty<RuntimeException>(AUTO_RECONFIGURE_ANALYSISIGNORE_PROPERTY, "false") {
      assertEquals(0, openProjectAndCountBinWarnings())
    }

    assertEquals(AnalysisIgnoreDefaults.BUILT_IN_LINES, AnalysisIgnoreDefaultsSettings.getInstance().lines)
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

    afterProjectClosed()
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
    afterProjectClosed()
    assertEquals(customLines, AnalysisIgnoreDefaultsSettings.getInstance().lines)
  }

  @Test
  fun `keeps the exclusions that the user customized`() {
    val customLines = AnalysisIgnoreDefaults.BUILT_IN_LINES + "/custom/"
    AnalysisIgnoreDefaultsSettings.getInstance().setLines(customLines)
    assertEquals(1, openProjectAndCountBinWarnings())

    afterProjectClosed()
    assertEquals(customLines, AnalysisIgnoreDefaultsSettings.getInstance().lines)
    runAnalysis()

    assertEquals(listOf("lib/lib.dart"), sarifResultPaths())
  }

  @Test
  fun `includes the bin directory when the customized exclusions do not exclude it`() {
    val customLines = (AnalysisIgnoreDefaults.BUILT_IN_LINES - "/bin/") + "/custom/"
    AnalysisIgnoreDefaultsSettings.getInstance().setLines(customLines)
    assertEquals(0, openProjectAndCountBinWarnings())

    afterProjectClosed()
    assertEquals(customLines, AnalysisIgnoreDefaultsSettings.getInstance().lines)
    runAnalysis()

    assertEquals(listOf("bin/main.dart", "lib/lib.dart"), sarifResultPaths())
  }

  private fun openProjectAndCountBinWarnings(): Int {
    val beforeWarnings = countBinWarnings { beforeProjectOpened() }
    assertEquals("The warning should wait until project configuration finishes", 0, beforeWarnings)
    return countBinWarnings { configureForQodana() }
  }

  private fun countBinWarnings(action: () -> Unit): Int {
    val output = ByteArrayOutputStream()
    val standardOut = System.out
    PrintStream(output, true, Charsets.UTF_8).use { stream ->
      try {
        System.setOut(stream)
        action()
      }
      finally {
        System.setOut(standardOut)
      }
    }
    val warning = "WARN - ${QodanaDartBundle.message("dart.analyze.warning.bin.excluded", qodanaConfig.projectPath)}"
    return output.toString(Charsets.UTF_8).lineSequence().count { it == warning }
  }

  private fun writeAnalysisIgnore(content: String): Path {
    val path = qodanaConfig.projectPath.resolve(".analysisignore")
    Files.writeString(path, content)
    val file = requireNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path))
    AnalysisIgnoreService.getInstance(project).applyNow(file)
    return path
  }
}