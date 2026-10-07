package org.jetbrains.qodana.dart

import com.intellij.openapi.vfs.LocalFileSystem
import com.jetbrains.qodana.sarif.model.Level
import com.jetbrains.qodana.sarif.SarifUtil
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

internal class DartAnalyzeSdkQodanaTest : DartAnalyzeProjectQodanaTestCase() {
  override val projectSourcePath: Path
    get() = if (getTestName(true) == "skips a project without Dart files") getTestDataPath("sources/test-module")
    else testData.resolve("dart-analyze/project")

  override val testDataBasePath: Path
    get() = Path.of(javaClass.simpleName, when (val testName = getTestName(true)) {
      "skips a project without Dart files" -> "noDartFiles"
      "skips Dart files outside only directory" -> "outsideOnlyDirectory"
      "skips both disabled Dart inspections" -> "disabledInspections"
      else -> testName
    })

  private var executable: DartExecutable = DartExecutable.Missing
  private var runnerCreations = 0
  private var versionResult = DartAnalyzeProcessResult(0, "Dart SDK version: test", "")
  private val commands = mutableListOf<List<String>>()

  override fun setUp() {
    super.setUp()
    updateQodanaConfig { it.copy(disableSanityInspections = false) }
  }

  override fun createRunnerFactory(executions: MutableList<Pair<String, String>>): DartAnalyzeRunnerFactory = DartAnalyzeRunnerFactory {
    runnerCreations++
    DartExecutableRunner(
      executableResolver = DartExecutableResolver { _, _ -> executable },
      processExecutor = object : DartAnalyzeProcessExecutor {
        override suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult {
          commands.add(command)
          return when (command[1]) {
            "--version" -> versionResult
            "analyze" -> DartAnalyzeProcessResult(0, "{\"diagnostics\":[]}", "")
            else -> error("Unexpected Dart command: $command")
          }
        }
      },
    )
  }

  @Test
  fun `reports a missing SDK once when both inspections are enabled`() {
    runAnalysis()

    assertSdkFailureInSarif("QODANA_FLUTTER_PATH")
    assertEquals(2, runnerCreations)
    assertEmpty(commands)
  }

  @Test
  fun `reports an unusable SDK as a sanity issue`() {
    executable = DartExecutable.Unusable("/missing/dart-sdk")

    runAnalysis()

    assertSdkFailureInSarif("/missing/dart-sdk")
  }

  @Test
  fun `reports an SDK failure for a file scope`() {
    updateQodanaConfig {
      it.copy(
        exclude = listOf(InspectScope("DartAnalyze")),
        include = listOf(InspectScope("DartAnalyze", paths = listOf("lib/main.dart"))),
      )
    }

    runAnalysis()

    assertSdkFailureInSarif("QODANA_FLUTTER_PATH")
  }

  @Test
  fun `skips a project without Dart files`() {
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
  fun `checks SDK when DartAnalyze is disabled`() {
    updateQodanaConfig { it.copy(exclude = listOf(InspectScope("DartAnalyze"))) }

    runAnalysis()

    assertSdkFailureInSarif("QODANA_FLUTTER_PATH")
    assertEquals(1, runnerCreations)
    assertEmpty(commands)
  }

  @Test
  fun `drops SDK failures when sanity is disabled`() {
    updateQodanaConfig { it.copy(disableSanityInspections = true) }

    runAnalysis()

    assertEmptySarifResults()
    assertNull(manager.sarifRun.properties?.get("qodana.sanity.results"))
    assertEquals(1, runnerCreations)
    assertEmpty(commands)
  }

  @Test
  fun `drops SDK failures when the sanity inspection is excluded`() {
    updateQodanaConfig { it.copy(exclude = listOf(InspectScope(DART_SANITY_INSPECTION_ID))) }

    runAnalysis()

    assertEmptySarifResults()
    assertNull(manager.sarifRun.properties?.get("qodana.sanity.results"))
    assertEquals(1, runnerCreations)
    assertEmpty(commands)
  }

  @Test
  fun `skips both disabled Dart inspections`() {
    updateQodanaConfig {
      it.copy(exclude = listOf(InspectScope("DartAnalyze"), InspectScope(DART_SANITY_INSPECTION_ID)))
    }

    runAnalysis()

    assertSkippedAnalysis()
  }

  @Test
  fun `reports a failed SDK probe once and skips analysis`() {
    executable = DartExecutable.Available(Path.of("dart"))
    versionResult = DartAnalyzeProcessResult(1, "", "SDK failed")

    runAnalysis()

    assertSdkFailureInSarif("SDK failed")
    // once in normal inspection, once in sanity (done so sanity inspection checks something by itself)
    // two errors reported under sanity inspection, duplicated results are filtered
    assertEquals(listOf(listOf("dart", "--version"), listOf("dart", "--version")), commands)
  }

  @Test
  fun `runs analysis once after successful SDK probes`() {
    executable = DartExecutable.Available(Path.of("dart"))

    runAnalysis()

    assertEmptySarifResults()
    assertNull(manager.sarifRun.properties?.get("qodana.sanity.results"))
    assertEquals(2, commands.count { it == listOf("dart", "--version") })
    assertEquals(1, commands.count { it == listOf("dart", "analyze", "--format=json", ".") })
  }

  @Test
  fun `reports an SDK failure when only directory contains Dart files`() {
    updateQodanaConfig { it.copy(onlyDirectory = Path.of("lib")) }

    runAnalysis()

    assertSdkFailureInSarif("QODANA_FLUTTER_PATH")
    assertEquals(2, runnerCreations)
  }

  private fun assertSkippedAnalysis() {
    assertSarifResults()
    assertEquals(0, runnerCreations)
    assertEmpty(manager.sarifRun.invocations.single().toolExecutionNotifications.orEmpty())
  }

  // framework filters out duplicates before comparing thus using dedicated checks here
  private fun assertSdkFailureInSarif(messageFragment: String) {
    assertEmpty(manager.sarifRun.results)
    val sanityResults = SarifUtil.readResultsFromObject(requireNotNull(manager.sarifRun.properties?.get("qodana.sanity.results")))
    assertEquals(1, sanityResults.size)
    val result = sanityResults.single()
    assertEquals(DART_SANITY_INSPECTION_ID, result.ruleId)
    assertEquals(Level.ERROR, result.level)
    assertEquals($$"file://$PROJECT_DIR$", result.singleLocation().physicalLocation.artifactLocation.uri)
    assertTrue(result.message.text.contains(messageFragment))
    assertEmpty(manager.sarifRun.invocations.single().toolExecutionNotifications.orEmpty())
  }
}
