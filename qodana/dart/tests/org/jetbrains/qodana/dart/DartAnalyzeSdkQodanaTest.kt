package org.jetbrains.qodana.dart

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.vfs.LocalFileSystem
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

internal class DartAnalyzeSdkQodanaTest : DartAnalyzeProjectQodanaTestCase() {
  override val projectSourcePath: Path
    get() = testData.resolve("DartAnalyzeGlobalInspectionQodanaTest/sources/test-module")

  override val testDataBasePath: Path
    get() = Path.of(javaClass.simpleName, getTestName(true))

  private var executable: DartExecutable = DartExecutable.Missing
  private var runnerCreations = 0

  override fun createRunnerFactory(executions: MutableList<Pair<String, String>>): DartAnalyzeRunnerFactory = DartAnalyzeRunnerFactory {
    runnerCreations++
    DartExecutableRunner(
      executableResolver = DartExecutableResolver { _, _ -> executable },
      processExecutor = object : DartAnalyzeProcessExecutor {
        override suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult =
          error("The analyzer must not run without an SDK")
      },
    )
  }

  @Test
  fun `reports a missing SDK as a DartAnalyze issue`() {
    runAnalysis()

    assertSdkFailure("QODANA_FLUTTER_PATH")
  }

  @Test
  fun `reports an unusable SDK as a DartAnalyze issue`() {
    executable = DartExecutable.Unusable("/missing/dart-sdk")

    runAnalysis()

    assertSdkFailure("/missing/dart-sdk")
  }

  @Test
  fun `skips a project without Dart files`() {
    val file = requireNotNull(LocalFileSystem.getInstance().findFileByNioFile(qodanaConfig.projectPath.resolve("lib/main.dart")))
    WriteAction.runAndWait<RuntimeException> { file.rename(this, "main.txt") }

    runAnalysis()

    assertSkippedAnalysis()
  }

  @Test
  fun `skips Dart files outside only directory`() {
    val directory = Files.createDirectory(qodanaConfig.projectPath.resolve("config"))
    LocalFileSystem.getInstance().refreshAndFindFileByNioFile(directory)
    updateQodanaConfig { it.copy(onlyDirectory = Path.of("config")) }

    runAnalysis()

    assertSkippedAnalysis()
  }

  @Test
  fun `skips excluded Dart files`() {
    updateQodanaConfig { it.copy(exclude = listOf(InspectScope("All", paths = listOf("lib/main.dart")))) }

    runAnalysis()

    assertSkippedAnalysis()
  }

  @Test
  fun `skips excluded Dart directories`() {
    updateQodanaConfig { it.copy(exclude = listOf(InspectScope("All", paths = listOf("lib")))) }

    runAnalysis()

    assertSkippedAnalysis()
  }

  @Test
  fun `skips a disabled Dart inspection`() {
    updateQodanaConfig { it.copy(exclude = listOf(InspectScope(DART_ANALYZE_SHORT_NAME))) }

    runAnalysis()

    assertSkippedAnalysis()
  }

  private fun assertSkippedAnalysis() {
    assertSarifResults()
    assertEquals(0, runnerCreations)
    assertEmpty(manager.sarifRun.invocations.single().toolExecutionNotifications.orEmpty())
  }

  private fun assertSdkFailure(messageFragment: String) {
    val result = singleSarifResult()
    assertEquals(DART_ANALYZE_SHORT_NAME, result.ruleId)
    assertTrue(result.message.text.contains(messageFragment))
    assertNull(manager.sarifRun.properties?.get("qodana.sanity.results"))
  }
}
