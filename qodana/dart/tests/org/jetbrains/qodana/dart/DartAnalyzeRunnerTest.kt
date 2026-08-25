package org.jetbrains.qodana.dart

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.NioFiles
import com.intellij.testFramework.TestDataPath
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

@TestDataPath($$"$CONTENT_ROOT/../test-data/dart-analyze")
class DartAnalyzeRunnerTest {
  @TempDir
  lateinit var tempDirectory: Path

  @Test
  fun `parses diagnostics and maps uncategorized codes to the aggregate inspection`() {
    val diagnostics = parseDartAnalyzeDiagnostics(resourceText("diagnostics.json"))

    assertEquals(3, diagnostics.size)
    val warningDiagnostic = diagnostics[0]
    assertEquals(DartAnalyzeLocation("lib/main.dart", 7, 12, 1), warningDiagnostic.location)
    val warning = warningDiagnostic.toExternalToolIssue(Path.of("/project"))
    assertEquals(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, warning.inspectionId)
    assertEquals(HighlightDisplayLevel.WARNING, warning.severity)
    assertEquals("[unused_import] Unused import: 'package:example/example.dart'.\nTry removing the import directive.", warning.description)
    assertEquals("file:///project/lib/main.dart", warning.file)
    assertEquals(1, warning.line)
    assertEquals(7, warning.offset)
    assertEquals(12, warning.length)
    assertEquals(HighlightDisplayLevel.ERROR, diagnostics[1].toExternalToolIssue(Path.of("/project")).severity)
    assertEquals(HighlightDisplayLevel.WEAK_WARNING, diagnostics[2].toExternalToolIssue(Path.of("/project")).severity)
  }

  @Test
  fun `parses empty diagnostics`() {
    assertTrue(parseDartAnalyzeDiagnostics(resourceText("no-diagnostics.json")).isEmpty())
  }

  @Test
  fun `formats paths relative to the project root for logging`() {
    val projectPath = Path.of("/project")

    assertEquals("subproject/lib/main.dart", "/project/subproject/lib/main.dart".toQodanaLogPath(projectPath))
    assertEquals("/tmp/external.dart", "/tmp/external.dart".toQodanaLogPath(projectPath))
    assertEquals("lib/main.dart", "lib/main.dart".toQodanaLogPath(projectPath))
  }

  @Test
  fun `uses the uncategorized inspection for diagnostic codes outside the catalog`() {
    val diagnostic = DartAnalyzeDiagnostic("future_diagnostic", "WARNING", "Future diagnostic", null, null)

    assertEquals(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, diagnostic.toExternalToolIssue(Path.of("/project")).inspectionId)
  }

  @Test
  fun `declares the uncategorized inspection disabled by default`() {
    val descriptor = DartAnalyzeInspectionCatalog.descriptors().single { it.shortName == DART_ANALYZE_UNCATEGORIZED_SHORT_NAME }

    assertFalse(descriptor.isEnabledByDefault)
    assertTrue(DartAnalyzeInspectionCatalog.descriptors().any { it.shortName == DART_PROJECT_SETUP_SHORT_NAME })
    assertFalse(DartAnalyzeInspectionCatalog.descriptors().any { it.shortName == DART_ANALYZE_SHORT_NAME })
  }

  @Test
  fun `maps package lints core and recommended rules to inspections`() {
    assertTrue("await_only_futures" in DartAnalyzeInspectionCatalog.coreLintCodes)
    assertTrue("annotate_overrides" in DartAnalyzeInspectionCatalog.recommendedLintCodes)
    assertTrue("avoid_print" in DartAnalyzeInspectionCatalog.flutterLintCodes)
    assertFalse("annotate_overrides" in DartAnalyzeInspectionCatalog.coreLintCodes)
    assertFalse("avoid_print" in DartAnalyzeInspectionCatalog.recommendedLintCodes)
    assertEquals("DartAnalyze.await_only_futures", DartAnalyzeInspectionCatalog.inspectionIdFor("await_only_futures"))
  }

  @Test
  fun `reports malformed JSON output`() = runBlocking {
    val result = DartExecutableRunner(
      processExecutor = FakeExecutor(DartAnalyzeProcessResult(0, "", ""), DartAnalyzeProcessResult(0, "not JSON", "")),
      executableResolver = availableExecutableResolver,
    ).run(Path.of("/project"))

    assertTrue(result is DartAnalyzeRunResult.Failure)
  }

  @Test
  fun `keeps diagnostics from nonzero Dart exit and analyzes the module from its directory`() = runBlocking {
    val executor = FakeExecutor(
      DartAnalyzeProcessResult(0, "", ""),
      DartAnalyzeProcessResult(2, resourceText("diagnostics.json"), "failure"),
    )
    val projectPath = Path.of("/project")
    val modulePath = projectPath.resolve("subproject")

    val runner = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver)
    assertEquals(DartPreparationResult.Success, runner.prepare(modulePath))
    val result = runner.analyze(modulePath)

    assertEquals(
      listOf(
        listOf("dart", "pub", "get"),
        listOf("dart", "analyze", "--format=json", "."),
      ),
      executor.commands,
    )
    assertEquals(listOf(modulePath, modulePath), executor.workingDirectories)
    val success = result as? DartAnalyzeRunResult.Success
    assertTrue(success != null)
    assertEquals(3, success?.diagnostics?.size)
  }

  @Test
  fun `uses Flutter executable commands for Flutter modules`() = runBlocking {
    val executor = FakeExecutor(
      DartAnalyzeProcessResult(0, "", ""),
      DartAnalyzeProcessResult(0, resourceText("no-diagnostics.json"), ""),
    )
    val projectPath = Path.of("/project")

    FlutterExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver).run(projectPath)

    assertEquals(
      listOf(
        listOf("flutter", "pub", "get"),
        listOf("dart", "analyze", "--format=json", "."),
      ),
      executor.commands,
    )
    assertEquals(listOf(projectPath, projectPath), executor.workingDirectories)
  }

  @Test
  fun `prepares dependencies without analyzing`() = runBlocking {
    val executor = FakeExecutor(DartAnalyzeProcessResult(0, "", ""))
    val projectPath = Path.of("/project")

    val result = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver).prepare(projectPath)

    assertEquals(DartPreparationResult.Success, result)
    assertEquals(listOf(listOf("dart", "pub", "get")), executor.commands)
    assertEquals(listOf(projectPath), executor.workingDirectories)
  }

  @Test
  fun `does not analyze a module when pub get fails`() = runBlocking {
    val executor = FakeExecutor(DartAnalyzeProcessResult(1, "", "Unable to resolve dependencies"))

    val result = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver).run(Path.of("/project"))

    assertTrue(result is DartAnalyzeRunResult.Failure)
    assertEquals(
      listOf(listOf("dart", "pub", "get")),
      executor.commands,
    )
  }

  @Test
  fun `reports a pub get failure with the executable exit code`() = runBlocking {
    val executor = FakeExecutor(
      DartAnalyzeProcessResult(1, "", "The lower bound of sdk must be 2.12.0 or higher to enable null safety."),
    )

    val result = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver).run(tempDirectory)

    val failure = result as? DartAnalyzeRunResult.Failure
    assertEquals(DART_PROJECT_SETUP_SHORT_NAME, failure?.inspectionId)
    assertTrue(failure?.message?.contains("must be 2.12.0 or higher to enable null safety") == true)
    assertTrue(failure?.message?.contains("exited with code 1") == true)
    assertEquals(listOf(listOf("dart", "pub", "get")), executor.commands)
  }

  @Test
  fun `finds Dart modules while ignoring metadata directories`() {
    val monorepoPath = copyMiniMonorepo()

    assertEquals(
      listOf(
        monorepoPath.resolve("apps/app"),
        monorepoPath.resolve("packages/core"),
        monorepoPath.resolve("packages/core/nested_flutter_app"),
        monorepoPath.resolve("packages/flutter_app"),
      ),
      findDartModuleDirectories(monorepoPath),
    )
  }

  @Test
  fun `detects Flutter modules`() {
    val flutterModule = copyMiniMonorepo().resolve("packages/flutter_app")

    assertTrue(isFlutterModule(flutterModule))
    assertEquals(DartModuleType.FLUTTER, findDartModules(flutterModule).single().type)
  }

  @Test
  fun `reports an unavailable executable`() = runBlocking {
    val result = DartExecutableRunner({ "/missing/dart" }, FakeExecutor()).run(Path.of("/project"))

    assertTrue(result is DartAnalyzeRunResult.Failure)
    assertTrue((result as DartAnalyzeRunResult.Failure).message.contains("not executable"))
  }

  @Test
  fun `reports missing executable configuration`() = runBlocking {
    val result = DartExecutableRunner({ null }, FakeExecutor()).run(Path.of("/project"))

    assertTrue(result is DartAnalyzeRunResult.Failure)
    assertTrue((result as DartAnalyzeRunResult.Failure).message.contains("must point to the SDK bin directory"))
  }

  @Test
  fun `reports an SDK directory without a dart executable`() = runBlocking {
    val sdkDirectory = Files.createDirectory(tempDirectory.resolve("dart-sdk"))

    val result = DartExecutableRunner({ sdkDirectory.toString() }, FakeExecutor()).prepare(sdkDirectory)

    assertTrue(result is DartPreparationResult.Failure)
    assertTrue((result as DartPreparationResult.Failure).failure.message.contains("not executable"))
  }

  @Test
  fun `analyzes Dart modules when the configured SDK has no Flutter executable`() = runBlocking {
    val sdkDirectory = Files.createDirectory(tempDirectory.resolve("dart-sdk"))
    val dartExecutableName = if (SystemInfo.isWindows) "dart.exe" else "dart"
    Files.createFile(sdkDirectory.resolve(dartExecutableName))
    val resolver = configuredExecutableResolver({ sdkDirectory.toString() }) { Files.isRegularFile(it) }
    val executor = FakeExecutor(
      DartAnalyzeProcessResult(0, "", ""),
      DartAnalyzeProcessResult(0, resourceText("no-diagnostics.json"), ""),
    )

    val dartResult = DartExecutableRunner(
      executablePath = { sdkDirectory.toString() },
      processExecutor = executor,
      executableResolver = resolver,
    ).run(sdkDirectory)
    val flutterPreparation = FlutterExecutableRunner(
      executablePath = { sdkDirectory.toString() },
      executableResolver = resolver,
    ).prepare(sdkDirectory)

    assertTrue(dartResult is DartAnalyzeRunResult.Success)
    assertTrue(flutterPreparation is DartPreparationResult.Failure)
    assertEquals(
      listOf(
        listOf(sdkDirectory.resolve(dartExecutableName).toString(), "pub", "get"),
        listOf(sdkDirectory.resolve(dartExecutableName).toString(), "analyze", "--format=json", "."),
      ),
      executor.commands,
    )
  }

  @Test
  fun `reports a blank executable configuration`() = runBlocking {
    val result = DartExecutableRunner({ "  " }, FakeExecutor()).run(Path.of("/project"))

    assertTrue(result is DartAnalyzeRunResult.Failure)
    assertTrue((result as DartAnalyzeRunResult.Failure).message.contains("must point to the SDK bin directory"))
  }

  @Test
  fun `resolves only the executable name supported by the current OS`() {
    val sdkDirectory = Files.createDirectory(tempDirectory.resolve("dart-sdk"))
    val supportedExecutableName = if (SystemInfo.isWindows) "dart.exe" else "dart"
    val unsupportedExecutableName = if (SystemInfo.isWindows) "dart" else "dart.exe"
    Files.createFile(sdkDirectory.resolve(unsupportedExecutableName))

    val unavailable = configuredExecutableResolver({ sdkDirectory.toString() }) { Files.isRegularFile(it) }.resolve("dart")
    assertEquals(DartExecutable.Unusable(sdkDirectory.toString()), unavailable)

    val dartExecutable = Files.createFile(sdkDirectory.resolve(supportedExecutableName))
    val result = configuredExecutableResolver({ sdkDirectory.toString() }) { Files.isRegularFile(it) }.resolve("dart")

    assertEquals(DartExecutable.Available(dartExecutable), result)
  }

  @Test
  fun `resolves Dart next to a configured Flutter executable`() {
    val sdkDirectory = Files.createDirectory(tempDirectory.resolve("flutter-sdk"))
    val flutterExecutableName = if (SystemInfo.isWindows) "flutter.exe" else "flutter"
    val dartExecutableName = if (SystemInfo.isWindows) "dart.exe" else "dart"
    Files.createFile(sdkDirectory.resolve(flutterExecutableName))
    val dartExecutable = Files.createFile(sdkDirectory.resolve(dartExecutableName))

    val result = configuredExecutableResolver({ sdkDirectory.resolve(flutterExecutableName).toString() }) { Files.isRegularFile(it) }
      .resolve("dart")

    assertEquals(DartExecutable.Available(dartExecutable), result)
  }

  @Test
  fun `reports an analyze failure without diagnostics`() = runBlocking {
    val executor = FakeExecutor(
      DartAnalyzeProcessResult(0, "", ""),
      DartAnalyzeProcessResult(3, "{\"diagnostics\":[]}", "Analyzer failed"),
    )

    val result = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver).run(Path.of("/project"))

    val failure = result as? DartAnalyzeRunResult.Failure
    assertTrue(failure?.message?.contains("dart analyze exited with code 3: Analyzer failed") == true)
    assertEquals(
      listOf(
        listOf("dart", "pub", "get"),
        listOf("dart", "analyze", "--format=json", "."),
      ),
      executor.commands,
    )
  }

  @Test
  fun `parses a diagnostic without a location`() {
    val diagnostic = parseDartAnalyzeDiagnostics(
      """
      {"diagnostics":[{"code":"todo","severity":"INFO","problemMessage":"TODO comment found."}]}
      """.trimIndent(),
    ).single()

    assertEquals(null, diagnostic.location)
    val issue = diagnostic.toExternalToolIssue(Path.of("/project"))
    assertEquals(null, issue.file)
    assertEquals(null, issue.line)
    assertEquals(null, issue.offset)
    assertEquals(null, issue.length)
  }

  @Test
  fun `reports process startup failures`() = runBlocking {
    val result = DartExecutableRunner(
      processExecutor = FailingExecutor(),
      executableResolver = availableExecutableResolver,
    ).run(Path.of("/project"))

    assertTrue(result is DartAnalyzeRunResult.Failure)
  }

  @Test
  fun `selects recorded responses by module path`() = runBlocking {
    val firstModule = tempDirectory.resolve("first-module")
    val secondModule = tempDirectory.resolve("second-module")
    Files.createDirectories(firstModule)
    Files.createDirectories(secondModule)
    val executor = RecordedDartAnalyzeProcessExecutor(
      tempDirectory,
      mapOf(
        "first-module" to RecordedDartAnalyzeResponse("no-diagnostics.json", resourceText("no-diagnostics.json")),
        "second-module" to RecordedDartAnalyzeResponse("diagnostics.json", resourceText("diagnostics.json")),
      ),
    )

    assertEquals(
      resourceText("diagnostics.json"),
      executor.execute(listOf("dart", "analyze"), secondModule).standardOutput,
    )
    assertEquals(
      resourceText("no-diagnostics.json"),
      executor.execute(listOf("dart", "analyze"), firstModule).standardOutput,
    )
  }

  private fun resourceText(name: String): String = requireNotNull(javaClass.getResource("/dart-analyze/$name")).readText()

  private fun copyMiniMonorepo(): Path {
    val source = Path.of(PluginPathManager.getPluginHomePath("qodana"), "dart", "test-data", "dart-analyze", "mini-monorepo")
    val target = tempDirectory.resolve("mini-monorepo")
    NioFiles.copyRecursively(source, target)
    Files.move(target.resolve("git"), target.resolve(".git"))
    return target
  }

  private val availableExecutableResolver = DartExecutableResolver { executableName ->
    DartExecutable.Available(Path.of(executableName))
  }

  private class FakeExecutor(vararg results: DartAnalyzeProcessResult) : DartAnalyzeProcessExecutor {
    private val results = ArrayDeque(results.asList())
    val commands = ArrayList<List<String>>()
    val workingDirectories = ArrayList<Path>()

    override suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult {
      commands.add(command)
      workingDirectories.add(workingDirectory)
      return results.removeFirst()
    }
  }

  private class FailingExecutor : DartAnalyzeProcessExecutor {
    override suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult {
      throw IOException("Unable to start Dart")
    }
  }
}
