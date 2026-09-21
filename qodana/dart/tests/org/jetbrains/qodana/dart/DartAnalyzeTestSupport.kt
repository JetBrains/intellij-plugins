package org.jetbrains.qodana.dart

import com.intellij.openapi.util.SystemInfo
import java.nio.file.Path

internal class RecordedDartAnalyzeProcessExecutor(
  private val projectPath: Path,
  private val analyzeResponses: Map<String, RecordedDartAnalyzeResponse>,
) : DartAnalyzeProcessExecutor {
  override suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult {
    val analysisDirectory = projectPath.toAbsolutePath().normalize().relativize(workingDirectory.toAbsolutePath().normalize()).portablePath()
    return when (command.getOrNull(1)) {
      "analyze" -> {
        val response = analyzeResponses[analysisDirectory] ?: error("No recorded Dart response for analysis directory '$analysisDirectory'")
        logResponse(analysisDirectory, "from fixture '${response.resourceName}'")
        DartAnalyzeProcessResult(0, response.content, "")
      }
      else -> error("Unexpected Dart command: $command")
    }
  }

  private fun logResponse(analysisDirectory: String, source: String) {
    println("Dart mock: using response for 'analyze' in analysis directory '$analysisDirectory' ($source)")
  }
}

private class RecordingDartAnalyzeProcessExecutor(
  private val delegate: DartAnalyzeProcessExecutor,
  private val projectPath: Path,
  private val executions: MutableList<Pair<String, String>>,
) : DartAnalyzeProcessExecutor {
  override suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult {
    val operation = command.getOrNull(1)?.takeIf { it == "analyze" }
    if (operation != null) {
      val analysisDirectory = projectPath.toAbsolutePath().normalize().relativize(workingDirectory.toAbsolutePath().normalize()).portablePath()
      executions += operation to analysisDirectory
    }
    return delegate.execute(command, workingDirectory)
  }
}

internal class DartAnalyzeTestRunnerFactory(
  projectPath: Path,
  analyzeResponses: Map<String, RecordedDartAnalyzeResponse>,
  executions: MutableList<Pair<String, String>> = mutableListOf(),
) {
  private val recordedProcessExecutor = RecordingDartAnalyzeProcessExecutor(
    RecordedDartAnalyzeProcessExecutor(projectPath, analyzeResponses),
    projectPath,
    executions,
  )
  private val nativeProcessExecutor = RecordingDartAnalyzeProcessExecutor(DefaultDartAnalyzeProcessExecutor, projectPath, executions)
  private val recordedExecutableResolver = DartExecutableResolver { executableName, _ ->
    DartExecutable.Available(Path.of(executableName))
  }

  fun create(): DartAnalyzeRunnerFactory = DartAnalyzeRunnerFactory {
    if (isConfiguredExecutable()) {
      DartExecutableRunner(processExecutor = nativeProcessExecutor)
    }
    else {
      logRecordedRunner()
      DartExecutableRunner(processExecutor = recordedProcessExecutor, executableResolver = recordedExecutableResolver)
    }
  }

  private fun isConfiguredExecutable(): Boolean =
    configuredExecutableResolver({ System.getenv(FLUTTER_PATH_ENV) }).resolve("dart", SystemInfo.isWindows) is DartExecutable.Available

  private fun logRecordedRunner() {
    println("Dart mock: using fallback runner for dart executable")
  }
}

internal object DartAnalyzeTestRunnerFactoryHolder {
  var factory: DartAnalyzeRunnerFactory? = null
}

internal class DartAnalyzeTestGlobalInspection : DartAnalyzeGlobalInspection(
  requireNotNull(DartAnalyzeTestRunnerFactoryHolder.factory) { "Dart test runner factory is not configured" },
)

internal data class RecordedDartAnalyzeResponse(val resourceName: String, val content: String)

internal fun readDartAnalyzeTestData(name: String): RecordedDartAnalyzeResponse = RecordedDartAnalyzeResponse(
  name,
  requireNotNull(RecordedDartAnalyzeProcessExecutor::class.java.getResource("/dart-analyze/$name")) {
    "Missing Dart test data '$name'"
  }.readText(),
)

private fun Path.portablePath(): String = toString().replace('\\', '/')
