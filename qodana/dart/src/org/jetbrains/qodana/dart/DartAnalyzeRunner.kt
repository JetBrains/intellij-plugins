package org.jetbrains.qodana.dart

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.platform.eel.ExecuteProcessException
import com.intellij.platform.eel.ThrowsChecked
import com.intellij.platform.eel.provider.asEelPath
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.toEelApi
import com.intellij.platform.eel.provider.utils.awaitProcessResult
import com.intellij.platform.eel.spawnProcess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolIssue
import java.nio.file.Files
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

internal const val FLUTTER_PATH_ENV = "QODANA_FLUTTER_PATH"

internal sealed interface DartExecutable {
  data class Available(val path: Path) : DartExecutable
  data object Missing : DartExecutable
  data class Unusable(val path: String) : DartExecutable
}

internal fun interface DartExecutableResolver {
  fun resolve(executableName: String): DartExecutable
}

internal abstract class DartAnalyzeRunner(
  private val executableResolver: DartExecutableResolver,
  private val processExecutor: DartAnalyzeProcessExecutor,
  private val preparationExecutableName: String,
  private val analysisExecutableName: String,
  private val executableEnv: String,
  private val parseDiagnostics: (String) -> List<DartAnalyzeDiagnostic>,
) {
  suspend fun prepare(projectPath: Path): DartPreparationResult {
    val executable = when (val result = executableResolver.resolve(preparationExecutableName)) {
      is DartExecutable.Available -> result.path
      else -> return DartPreparationResult.Failure(result.unavailable(preparationExecutableName, executableEnv))
    }
    val pubGetResult = try {
      processExecutor.execute(listOf(executable.toString(), "pub", "get"), projectPath)
    }
    catch (e: CancellationException) {
      throw e
    }
    catch (e: Exception) {
      return DartPreparationResult.Failure(DartAnalyzeRunResult.Failure(QodanaDartBundle.message("dart.analyze.error.pub.get.process", preparationExecutableName, e.message ?: e.javaClass.name)))
    }
    LOG.info("$preparationExecutableName pub get finished with exit code ${pubGetResult.exitCode}; stdout: ${pubGetResult.standardOutput.logValue()}; stderr: ${pubGetResult.standardError.logValue()}")
    if (pubGetResult.exitCode != 0) {
      LOG.warn("$preparationExecutableName pub get failed with exit code ${pubGetResult.exitCode}")
      LOG.warn(pubGetResult.standardError)
      val output = pubGetResult.standardError.ifBlank { pubGetResult.standardOutput }.trim().take(1_000)
      return DartPreparationResult.Failure(DartAnalyzeRunResult.Failure(
        QodanaDartBundle.message("dart.analyze.error.pub.get.exit", preparationExecutableName, pubGetResult.exitCode, output),
      ))
    }
    return DartPreparationResult.Success
  }

  suspend fun analyze(modulePath: Path): DartAnalyzeRunResult {
    val executable = when (val result = executableResolver.resolve(analysisExecutableName)) {
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

  suspend fun run(projectPath: Path): DartAnalyzeRunResult {
    val preparationResult = prepare(projectPath)
    if (preparationResult is DartPreparationResult.Failure) return preparationResult.failure
    return analyze(projectPath)
  }

  private fun String.logValue(): String = replace("\n", "\\n").replace("\r", "\\r").take(LOG_OUTPUT_LIMIT)

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
  isExecutable: (Path) -> Boolean = { Files.isRegularFile(it) && Files.isExecutable(it) },
): DartExecutableResolver = DartExecutableResolver { executableName ->
  val configuredPath = executablePath()?.takeIf(String::isNotBlank) ?: return@DartExecutableResolver DartExecutable.Missing
  val configuredPathValue = runCatching { Path.of(configuredPath) }.getOrNull() ?: return@DartExecutableResolver DartExecutable.Unusable(configuredPath)
  val executableCandidates = if (Files.isDirectory(configuredPathValue)) {
    executableNames(executableName).map(configuredPathValue::resolve)
  }
  else {
    executablePaths(configuredPathValue, executableName)
  }
  executableCandidates.firstOrNull(isExecutable)?.let(DartExecutable::Available)
    ?: DartExecutable.Unusable(configuredPath)
}

internal fun executableNames(executableName: String): List<String> =
  if (SystemInfo.isWindows) listOf("$executableName.exe") else listOf(executableName)

private fun executablePaths(configuredPath: Path, executableName: String): List<Path> = buildList {
  val configuredFileName = configuredPath.fileName?.toString()
  if (executableNames(executableName).any { it.equals(configuredFileName, ignoreCase = true) }) {
    add(configuredPath)
  }
  configuredPath.parent?.let { parent -> executableNames(executableName).mapTo(this, parent::resolve) }
}

internal class DartExecutableRunner(
  executablePath: () -> String? = { System.getenv(FLUTTER_PATH_ENV) },
  processExecutor: DartAnalyzeProcessExecutor = DefaultDartAnalyzeProcessExecutor,
  executableResolver: DartExecutableResolver = configuredExecutableResolver(executablePath),
) : DartAnalyzeRunner(
  executableResolver,
  processExecutor,
  "dart",
  "dart",
  FLUTTER_PATH_ENV,
  ::parseDartAnalyzeDiagnostics,
)

internal class FlutterExecutableRunner(
  executablePath: () -> String? = { System.getenv(FLUTTER_PATH_ENV) },
  processExecutor: DartAnalyzeProcessExecutor = DefaultDartAnalyzeProcessExecutor,
  executableResolver: DartExecutableResolver = configuredExecutableResolver(executablePath),
) : DartAnalyzeRunner(
  executableResolver,
  processExecutor,
  "flutter",
  "dart",
  FLUTTER_PATH_ENV,
  ::parseDartAnalyzeDiagnostics,
)

private fun DartExecutable.toFailure(envName: String): DartAnalyzeRunResult.Failure = when (this) {
  DartExecutable.Missing -> DartAnalyzeRunResult.Failure(QodanaDartBundle.message("dart.analyze.error.executable.not.configured", envName))
  is DartExecutable.Unusable -> DartAnalyzeRunResult.Failure(QodanaDartBundle.message("dart.analyze.error.executable.not.executable", path))
  is DartExecutable.Available -> error("An available executable cannot produce a failure")
}

internal fun findDartModuleDirectories(projectPath: Path): List<Path> {
  val moduleDirectories = ArrayList<Path>()
  Files.walkFileTree(projectPath, object : SimpleFileVisitor<Path>() {
    override fun preVisitDirectory(directory: Path, attributes: BasicFileAttributes): FileVisitResult {
      return if (directory != projectPath && directory.fileName.toString().startsWith('.')) {
        FileVisitResult.SKIP_SUBTREE
      }
      else {
        FileVisitResult.CONTINUE
      }
    }

    override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
      if (file.fileName.toString() == "pubspec.yaml") {
        moduleDirectories.add(file.parent)
      }
      return FileVisitResult.CONTINUE
    }

    override fun visitFileFailed(file: Path, exception: java.io.IOException): FileVisitResult = FileVisitResult.CONTINUE
  })
  return moduleDirectories.sorted()
}

private val FLUTTER_MODULE_DECLARATION = Regex("""^\s*(?:sdk\s*:\s*['"]?flutter['"]?\s*(?:#.*)?|flutter\s*:.*)$""")

internal fun isFlutterModule(moduleDirectory: Path): Boolean = runCatching {
  Files.readAllLines(moduleDirectory.resolve("pubspec.yaml")).any(FLUTTER_MODULE_DECLARATION::matches)
}.getOrDefault(false)

internal sealed interface DartAnalyzeRunResult {
  data class Success(val diagnostics: List<DartAnalyzeDiagnostic>) : DartAnalyzeRunResult
  data class Failure(val message: String, val inspectionId: String = DART_PROJECT_SETUP_SHORT_NAME) : DartAnalyzeRunResult
}

internal sealed interface DartPreparationResult {
  data object Success : DartPreparationResult
  data class Failure(val failure: DartAnalyzeRunResult.Failure) : DartPreparationResult
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

internal data class DartAnalyzeDiagnostic(
  val code: String,
  val severity: String,
  val message: String,
  val correction: String?,
  val location: DartAnalyzeLocation?,
) {
  fun toExternalToolIssue(projectPath: Path): ExternalToolIssue {
    val location = location
    return ExternalToolIssue(
      DartAnalyzeInspectionCatalog.inspectionIdFor(code),
      severity.toHighlightDisplayLevel(),
      buildString {
        append('[').append(code).append("] ").append(message)
        correction?.takeIf(String::isNotBlank)?.let { append('\n').append(it) }
      },
      location?.file?.toFileUrl(projectPath),
      location?.startLine,
      location?.offset,
      location?.length,
    )
  }
}

internal data class DartAnalyzeLocation(
  val file: String?,
  val offset: Int?,
  val length: Int?,
  val startLine: Int?,
  val startColumn: Int? = null,
  val endLine: Int? = null,
  val endColumn: Int? = null,
)

internal fun parseDartAnalyzeDiagnostics(json: String): List<DartAnalyzeDiagnostic> {
  val root = JsonParser.parseString(json).asJsonObject
  val diagnostics = root.requiredArray("diagnostics")
  return diagnostics.map { element ->
    val diagnostic = element.asJsonObject
    DartAnalyzeDiagnostic(
      diagnostic.requiredString("code"),
      diagnostic.requiredString("severity"),
      diagnostic.requiredString("problemMessage"),
      diagnostic.string("correctionMessage"),
      diagnostic.objectOrNull("location")?.toDartAnalyzeLocation(),
    )
  }
}

private fun JsonObject.toDartAnalyzeLocation(): DartAnalyzeLocation {
  val range = objectOrNull("range")
  val start = range?.objectOrNull("start")
  val end = range?.objectOrNull("end")
  val offset = start?.intOrNull("offset")
  val length = offset?.let { startOffset -> end?.intOrNull("offset")?.minus(startOffset) }
  return DartAnalyzeLocation(
    string("file"),
    offset,
    length,
    start?.intOrNull("line"),
    start?.intOrNull("column"),
    end?.intOrNull("line"),
    end?.intOrNull("column"),
  )
}

private fun JsonObject.requiredArray(name: String): JsonArray = get(name)?.takeIf { it.isJsonArray }?.asJsonArray
  ?: error("Missing '$name' array")

private fun JsonObject.requiredString(name: String): String = string(name) ?: error("Missing '$name' string")

private fun JsonObject.string(name: String): String? = get(name)?.takeUnless { it.isJsonNull }?.asString

private fun JsonObject.intOrNull(name: String): Int? = get(name)?.takeUnless { it.isJsonNull }?.asInt

private fun JsonObject.objectOrNull(name: String): JsonObject? = get(name)?.takeIf { it.isJsonObject }?.asJsonObject

private fun String.toHighlightDisplayLevel(): HighlightDisplayLevel = when (uppercase()) {
  "ERROR" -> HighlightDisplayLevel.ERROR
  "WARNING" -> HighlightDisplayLevel.WARNING
  else -> HighlightDisplayLevel.WEAK_WARNING
}

internal fun String.toFileUrl(projectPath: Path): String? = runCatching {
  val path = Path.of(this)
  VfsUtilCore.pathToUrl((if (path.isAbsolute) path else projectPath.resolve(path)).toString())
}.getOrNull()
