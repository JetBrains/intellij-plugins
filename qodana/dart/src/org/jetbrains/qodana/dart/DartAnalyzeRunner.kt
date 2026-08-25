package org.jetbrains.qodana.dart

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.platform.eel.ExecuteProcessException
import com.intellij.platform.eel.ThrowsChecked
import com.intellij.platform.eel.isWindows
import com.intellij.platform.eel.provider.asEelPath
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.toEelApi
import com.intellij.platform.eel.provider.utils.awaitProcessResult
import com.intellij.platform.eel.spawnProcess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import java.nio.file.Files
import java.nio.file.Path

internal const val FLUTTER_PATH_ENV = "QODANA_FLUTTER_PATH"

internal sealed interface DartExecutable {
  data class Available(val path: Path) : DartExecutable
  data object Missing : DartExecutable
  data class Unusable(val path: String) : DartExecutable
}

internal fun interface DartExecutableResolver {
  fun resolve(executableName: String, isWindows: Boolean): DartExecutable
}

internal abstract class DartAnalyzeRunner(
  private val executableResolver: DartExecutableResolver,
  private val processExecutor: DartAnalyzeProcessExecutor,
  private val analysisExecutableName: String,
  private val executableEnv: String,
  private val parseDiagnostics: (String) -> List<DartAnalyzeDiagnostic>,
) {
  suspend fun analyze(modulePath: Path): DartAnalyzeRunResult {
    val executable = when (val result = resolveExecutable(analysisExecutableName, modulePath)) {
      is DartExecutable.Available -> result.path
      else -> return result.unavailable(analysisExecutableName, executableEnv)
    }
    val analyzeResult = try {
      processExecutor.execute(
        listOf(executable.toString(), "analyze", "--format=json", "."),
        modulePath,
      )
    }
    catch (e: CancellationException) {
      throw e
    }
    catch (e: Exception) {
      return DartAnalyzeRunResult.Failure(QodanaDartBundle.message("dart.analyze.error.process", analysisExecutableName, e.message ?: e.javaClass.name))
    }
    LOG.info("$analysisExecutableName analyze finished with exit code ${analyzeResult.exitCode}; stdout: ${analyzeResult.standardOutput.logValue()}; stderr: ${analyzeResult.standardError.logValue()}")
    val diagnostics = try {
      parseDiagnostics(analyzeResult.standardOutput)
    }
    catch (e: Exception) {
      return DartAnalyzeRunResult.Failure(QodanaDartBundle.message("dart.analyze.error.invalid.output", analysisExecutableName, e.message ?: e.javaClass.name))
    }
    if (analyzeResult.exitCode != 0 && diagnostics.isEmpty()) {
      LOG.warn("$analysisExecutableName analyze finished with exit code ${analyzeResult.exitCode}")
      LOG.warn(analyzeResult.standardOutput)
      val output = analyzeResult.standardError.ifBlank { analyzeResult.standardOutput }.trim().take(1_000)
      return DartAnalyzeRunResult.Failure(QodanaDartBundle.message("dart.analyze.error.exit", analysisExecutableName, analyzeResult.exitCode, output))
    }
    return DartAnalyzeRunResult.Success(diagnostics)
  }

  private fun String.logValue(): String = replace("\n", "\\n").replace("\r", "\\r").take(LOG_OUTPUT_LIMIT)

  private suspend fun resolveExecutable(executableName: String, modulePath: Path): DartExecutable =
    executableResolver.resolve(executableName, modulePath.getEelDescriptor().toEelApi().platform.isWindows)

  private fun DartExecutable.unavailable(executableName: String, envName: String): DartAnalyzeRunResult.Failure =
    toFailure(envName).also { LOG.warn("$executableName executable is not available: ${it.message}") }
}

private const val LOG_OUTPUT_LIMIT = 4_000

internal fun Path.toQodanaLogPath(projectPath: Path): String = runCatching {
  val absoluteProjectPath = projectPath.toAbsolutePath().normalize()
  val absolutePath = toAbsolutePath().normalize()
  absolutePath.takeIf { it.startsWith(absoluteProjectPath) }
    ?.let { absoluteProjectPath.relativize(it).toString().ifEmpty { "." } }
    ?: toString()
}.getOrElse { toString() }

internal fun String.toQodanaLogPath(projectPath: Path): String = runCatching {
  val path = Path.of(this)
  if (path.isAbsolute) path.toQodanaLogPath(projectPath) else this
}.getOrElse { this }

private val LOG = Logger.getInstance(DartAnalyzeRunner::class.java)

internal fun configuredExecutableResolver(
  executablePath: () -> String?,
  pathExecutableResolver: (String) -> Path? = PathEnvironmentVariableUtil::findFirst,
  isExecutable: (Path) -> Boolean = { Files.isRegularFile(it) && Files.isExecutable(it) },
): DartExecutableResolver = DartExecutableResolver { executableName, isWindows ->
  val configuredPath = executablePath()?.takeIf(String::isNotBlank)
    ?: return@DartExecutableResolver pathExecutableResolver(executableName)?.let(DartExecutable::Available) ?: DartExecutable.Missing
  val configuredPathValue = runCatching { Path.of(configuredPath) }.getOrNull() ?: return@DartExecutableResolver DartExecutable.Unusable(configuredPath)
  val executableNames = executableNames(executableName, isWindows)
  val executableCandidates = if (Files.isDirectory(configuredPathValue)) {
    executableNames.map(configuredPathValue::resolve)
  }
  else {
    executablePaths(configuredPathValue, executableNames)
  }
  executableCandidates.firstOrNull(isExecutable)?.let(DartExecutable::Available)
    ?: DartExecutable.Unusable(configuredPath)
}

internal fun executableNames(executableName: String, isWindows: Boolean): List<String> =
  if (isWindows) listOf("$executableName.bat", "$executableName.exe") else listOf(executableName)

private fun executablePaths(configuredPath: Path, executableNames: List<String>): List<Path> = buildList {
  val configuredFileName = configuredPath.fileName?.toString()
  if (executableNames.any { it.equals(configuredFileName, ignoreCase = true) }) {
    add(configuredPath)
  }
  configuredPath.parent?.let { parent -> executableNames.mapTo(this, parent::resolve) }
}

internal class DartExecutableRunner(
  executablePath: () -> String? = { System.getenv(FLUTTER_PATH_ENV) },
  processExecutor: DartAnalyzeProcessExecutor = DefaultDartAnalyzeProcessExecutor,
  executableResolver: DartExecutableResolver = configuredExecutableResolver(executablePath),
) : DartAnalyzeRunner(
  executableResolver,
  processExecutor,
  "dart",
  FLUTTER_PATH_ENV,
  ::parseDartAnalyzeDiagnostics,
)

private fun DartExecutable.toFailure(envName: String): DartAnalyzeRunResult.Failure = when (this) {
  DartExecutable.Missing -> DartAnalyzeRunResult.Failure(QodanaDartBundle.message("dart.analyze.error.executable.not.configured", envName))
  is DartExecutable.Unusable -> DartAnalyzeRunResult.Failure(QodanaDartBundle.message("dart.analyze.error.executable.not.executable", path))
  is DartExecutable.Available -> error("An available executable cannot produce a failure")
}

internal sealed interface DartAnalyzeRunResult {
  data class Success(val diagnostics: List<DartAnalyzeDiagnostic>) : DartAnalyzeRunResult
  data class Failure(val message: String, val inspectionId: String = DART_ANALYZE_SHORT_NAME) : DartAnalyzeRunResult
}

internal interface DartAnalyzeProcessExecutor {
  suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult
}

internal data class DartAnalyzeProcessResult(
  val exitCode: Int,
  val standardOutput: String,
  val standardError: String,
)

internal object DefaultDartAnalyzeProcessExecutor : DartAnalyzeProcessExecutor {
  @ThrowsChecked(ExecuteProcessException::class)
  override suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult = coroutineScope {
    val process = workingDirectory.getEelDescriptor().toEelApi().exec.spawnProcess(command.first())
      .args(command.drop(1))
      .workingDirectory(workingDirectory.asEelPath())
      .scope(this)
      .eelIt()
    val result = process.awaitProcessResult()
    DartAnalyzeProcessResult(
      result.exitCode,
      result.stdout.decodeToString(),
      result.stderr.decodeToString(),
    )
  }
}
