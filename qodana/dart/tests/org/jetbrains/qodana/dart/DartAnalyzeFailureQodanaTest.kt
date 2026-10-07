package org.jetbrains.qodana.dart

import com.intellij.openapi.components.service
import com.intellij.testFramework.LoggedErrorProcessor
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.Invocation
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.RuntimeNotificationCollector
import org.junit.Test
import java.io.IOException
import java.io.StringWriter
import java.nio.file.Path

internal class DartAnalyzeFailureQodanaTest : DartAnalyzeProjectQodanaTestCase() {
  override val testDataBasePath: Path = Path.of("failures")

  override val projectSourcePath: Path
    get() = testData.resolve("dart-analyze/project")

  private var analyzeResult = DartAnalyzeProcessResult(0, "{\"diagnostics\":[]}", "")
  private var processFailure: Exception? = null
  private var executableAvailable = true
  private var removeExecutableAfterProbe = false

  override fun createRunnerFactory(executions: MutableList<Pair<String, String>>): DartAnalyzeRunnerFactory = DartAnalyzeRunnerFactory {
    DartExecutableRunner(
      executableResolver = DartExecutableResolver { _, _ ->
        if (executableAvailable) DartExecutable.Available(Path.of("dart")) else DartExecutable.Missing
      },
      processExecutor = object : DartAnalyzeProcessExecutor {
        override suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult {
          if (command[1] == "--version") {
            if (removeExecutableAfterProbe) executableAvailable = false
            return DartAnalyzeProcessResult(0, "Dart SDK version: test", "")
          }
          processFailure?.let { throw it }
          return analyzeResult
        }
      },
    )
  }

  @Test
  fun `reports process failures as tool errors and preserves the cause`() {
    val message = "Unable to start Dart: \"quoted\" \\path\nsecond line\r\t\u0001"
    processFailure = IOException(message)

    assertFailureSarif("process")
  }

  @Test
  fun `reports malformed output as a tool error`() {
    analyzeResult = DartAnalyzeProcessResult(0, "not JSON", "Analyzer stderr")

    assertFailureSarif("output")
  }

  @Test
  fun `reports a nonzero exit without diagnostics as a tool error`() {
    analyzeResult = DartAnalyzeProcessResult(3, "{\"diagnostics\":[]}", "Analyzer failed")

    assertFailureSarif("exit")
  }

  @Test
  fun `reports an executable disappearing after the SDK check as a tool error`() {
    removeExecutableAfterProbe = true

    assertFailureSarif("executable")
  }

  private fun assertFailureSarif(caseName: String) {
    runBeforeAnalysis { config, project -> project.service<RuntimeNotificationCollector>().initializeForRun(config) }
    LoggedErrorProcessor.executeWith<Nothing>(object : LoggedErrorProcessor() {
      override fun processError(category: String, message: String, details: Array<out String>, t: Throwable?): Set<Action> =
        if (t is IllegalStateException && t.message.orEmpty().startsWith("Dart module '")) Action.NONE
        else super.processError(category, message, details, t)
    }, ::runAnalysis)

    val run = SarifUtil.readReport(qodanaConfig.outPath.resolve("qodana.sarif.json")).runs.single()
    assertEmpty(run.results.orEmpty())
    assertNull(run.properties?.get("qodana.sanity.results"))
    val notifications = run.invocations.single().toolExecutionNotifications
    notifications.forEach { notification ->
      notification.timeUtc = null
      notification.exception.message = notification.exception.message
        .replace(qodanaConfig.projectPath.toString(), $$"$PROJECT_DIR$")
        .split('\n')
        .filterNot { it.startsWith("\tat ") || it.startsWith("\t... ") }
        .joinToString("\n")
    }
    val report = SarifReport().withRuns(listOf(
      Run().withResults(run.results).withInvocations(listOf(Invocation().withToolExecutionNotifications(notifications))),
    ))
    val writer = StringWriter()
    SarifUtil.writeReport(writer, report)
    assertSameLinesWithFile(getTestDataPath("expected/$caseName.sarif.json").toString(), writer.toString())
  }
}
