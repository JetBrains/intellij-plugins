package org.jetbrains.qodana.dart

import com.intellij.openapi.progress.ProcessCanceledException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class DartAnalyzeRunnerEdgeCasesTest {
  @TempDir
  lateinit var tempDirectory: Path

  @Test
  fun `rethrows ProcessCanceledException during pub get`() = runBlocking {
    val executor = CanceledExecutor(cancelOnCall = 1)

    val runner = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver)

    val thrown = try {
      runner.prepare(tempDirectory)
      false
    } catch (_: ProcessCanceledException) {
      true
    }
    assertTrue(thrown, "ProcessCanceledException should be re-thrown during pub get")
    assertEquals(1, executor.callCount)
  }

  @Test
  fun `rethrows ProcessCanceledException during analyze`() = runBlocking {
    val executor = CanceledExecutor(cancelOnCall = 2)

    val runner = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver)
    runner.prepare(tempDirectory)

    val thrown = try {
      runner.analyze(tempDirectory)
      false
    } catch (_: ProcessCanceledException) {
      true
    }
    assertTrue(thrown, "ProcessCanceledException should be re-thrown during analyze")
    assertEquals(2, executor.callCount)
  }

  @Test
  fun `finds no modules in empty project`() {
    val emptyProject = tempDirectory.resolve("empty-project")
    Files.createDirectory(emptyProject)

    assertTrue(findDartModuleDirectories(emptyProject).isEmpty())
  }

  @Test
  fun `finds no modules when only ignored directories exist`() {
    val project = tempDirectory.resolve("project")
    Files.createDirectories(project.resolve(".dart_tool"))
    Files.createDirectories(project.resolve(".git"))

    assertTrue(findDartModuleDirectories(project).isEmpty())
  }

  @Test
  fun `isFlutterModule returns false for malformed pubspec`() {
    val module = tempDirectory.resolve("malformed")
    Files.createDirectory(module)
    Files.writeString(module.resolve("pubspec.yaml"), "")

    assertFalse(isFlutterModule(module))
  }

  @Test
  fun `isFlutterModule returns false when pubspec missing`() {
    val module = tempDirectory.resolve("no-pubspec")
    Files.createDirectory(module)

    assertFalse(isFlutterModule(module))
  }

  @Test
  fun `isFlutterModule returns false for non-flutter pubspec`() {
    val module = tempDirectory.resolve("dart-only")
    Files.createDirectory(module)
    Files.writeString(module.resolve("pubspec.yaml"), """
      name: my_app
      environment:
        sdk: ">=2.12.0 <4.0.0"
    """.trimIndent())

    assertFalse(isFlutterModule(module))
  }

  @Test
  fun `isFlutterModule returns true for flutter sdk constraint`() {
    val module = tempDirectory.resolve("flutter-app")
    Files.createDirectory(module)
    Files.writeString(module.resolve("pubspec.yaml"), """
      name: my_flutter_app
      environment:
        sdk: '>=2.12.0 <4.0.0'
      dependencies:
        sdk: flutter
    """.trimIndent())

    assertTrue(isFlutterModule(module))
  }

  @Test
  fun `isFlutterModule returns true for flutter section`() {
    val module = tempDirectory.resolve("flutter-section")
    Files.createDirectory(module)
    Files.writeString(module.resolve("pubspec.yaml"), """
      name: my_flutter_app
      flutter:
        uses-sdk: flutter
    """.trimIndent())

    assertTrue(isFlutterModule(module))
  }

  @Test
  fun `parseDartAnalyzeDiagnostics throws on missing diagnostics array`() {
    val thrown = try {
      parseDartAnalyzeDiagnostics("{}")
      false
    } catch (_: Exception) {
      true
    }
    assertTrue(thrown, "Should throw on missing diagnostics array")
  }

  @Test
  fun `parseDartAnalyzeDiagnostics throws on missing required fields`() {
    val thrown = try {
      parseDartAnalyzeDiagnostics("""{"diagnostics":[{"severity":"ERROR"}]}""")
      false
    } catch (_: Exception) {
      true
    }
    assertTrue(thrown, "Should throw on missing required field 'code'")
  }

  @Test
  fun `Path toQodanaLogPath handles absolute path inside project`() {
    val projectPath = Path.of("/project")

    assertEquals("lib/main.dart", Path.of("/project/lib/main.dart").toQodanaLogPath(projectPath))
  }

  @Test
  fun `Path toQodanaLogPath handles path outside project`() {
    val projectPath = Path.of("/project")

    assertEquals("/tmp/external.dart", Path.of("/tmp/external.dart").toQodanaLogPath(projectPath))
  }

  @Test
  fun `Path toQodanaLogPath handles path equal to project root`() {
    val projectPath = Path.of("/project")

    assertEquals(".", Path.of("/project").toQodanaLogPath(projectPath))
  }

  @Test
  fun `String toQodanaLogPath handles non-absolute path`() {
    val projectPath = Path.of("/project")

    assertEquals("lib/main.dart", "lib/main.dart".toQodanaLogPath(projectPath))
  }

  @Test
  fun `toFileUrl handles absolute path`() {
    val projectPath = Path.of("/project")

    val url = "/project/lib/main.dart".toFileUrl(projectPath)

    assertTrue(url?.startsWith("file://") == true)
    assertTrue(url?.contains("lib/main.dart") == true)
  }

  @Test
  fun `toFileUrl handles relative path`() {
    val projectPath = Path.of("/project")

    val url = "lib/main.dart".toFileUrl(projectPath)

    assertTrue(url?.startsWith("file://") == true)
    assertTrue(url?.contains("project/lib/main.dart") == true)
  }

  @Test
  fun `toFileUrl returns null for malformed path`() {
    val projectPath = Path.of("/project")

    val url = "\u0000invalid".toFileUrl(projectPath)

    assertNull(url)
  }

  @Test
  fun `Flutter runner uses flutter pub get and dart analyze`() = runBlocking {
    val executor = FakeExecutor(
      DartAnalyzeProcessResult(0, "", ""),
      DartAnalyzeProcessResult(0, noDiagnosticsResourceText(), ""),
    )

    FlutterExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver).run(tempDirectory)

    assertEquals(
      listOf(
        listOf("flutter", "pub", "get"),
        listOf("dart", "analyze", "--format=json", "."),
      ),
      executor.commands,
    )
  }

  @Test
  fun `Dart runner does not run flutter pub get`() = runBlocking {
    val executor = FakeExecutor(
      DartAnalyzeProcessResult(0, "", ""),
      DartAnalyzeProcessResult(0, noDiagnosticsResourceText(), ""),
    )

    DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver).run(tempDirectory)

    assertEquals(
      listOf(
        listOf("dart", "pub", "get"),
        listOf("dart", "analyze", "--format=json", "."),
      ),
      executor.commands,
    )
  }

  private fun noDiagnosticsResourceText(): String = requireNotNull(javaClass.getResource("/dart-analyze/no-diagnostics.json")).readText()

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

  private class CanceledExecutor(
    private val cancelOnCall: Int,
  ) : DartAnalyzeProcessExecutor {
    var callCount = 0

    override suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult {
      callCount++
      if (callCount >= cancelOnCall) throw ProcessCanceledException()
      return DartAnalyzeProcessResult(0, "", "")
    }
  }
}
