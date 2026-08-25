package org.jetbrains.qodana.dart

import com.intellij.openapi.util.SystemInfo
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class DartExecutableResolverTest {
  @TempDir
  lateinit var tempDirectory: Path

  @Test
  fun `reports an unavailable executable`() = runBlocking {
    val result = DartExecutableRunner({ "/missing/dart" }, FakeExecutor()).analyze(tempDirectory)

    assertTrue(result is DartAnalyzeRunResult.Failure)
    assertTrue((result as DartAnalyzeRunResult.Failure).message.contains("not executable"))
  }

  @Test
  fun `reports missing executable configuration`() = runBlocking {
    val result = runnerWithoutExecutable { null }.analyze(tempDirectory)

    assertTrue(result is DartAnalyzeRunResult.Failure)
    assertTrue((result as DartAnalyzeRunResult.Failure).message.contains("must point to the SDK bin directory"))
  }

  @Test
  fun `reports a blank executable configuration`() = runBlocking {
    val result = runnerWithoutExecutable { "  " }.analyze(tempDirectory)

    assertTrue(result is DartAnalyzeRunResult.Failure)
    assertTrue((result as DartAnalyzeRunResult.Failure).message.contains("must point to the SDK bin directory"))
  }

  @Test
  fun `reports an SDK directory without a dart executable`() = runBlocking {
    val sdkDirectory = Files.createDirectory(tempDirectory.resolve("dart-sdk"))

    val result = DartExecutableRunner({ sdkDirectory.toString() }, FakeExecutor()).analyze(sdkDirectory)

    assertTrue(result is DartAnalyzeRunResult.Failure)
    assertTrue((result as DartAnalyzeRunResult.Failure).message.contains("not executable"))
  }

  @Test
  fun `analyzes a module with the configured Dart SDK`() = runBlocking {
    val sdkDirectory = Files.createDirectory(tempDirectory.resolve("dart-sdk"))
    val dartExecutableName = if (SystemInfo.isWindows) "dart.exe" else "dart"
    Files.createFile(sdkDirectory.resolve(dartExecutableName))
    val resolver = configuredExecutableResolver({ sdkDirectory.toString() }) { Files.isRegularFile(it) }
    val executor = FakeExecutor(DartAnalyzeProcessResult(0, noDiagnosticsResourceText(), ""))

    val dartResult = DartExecutableRunner(
      executablePath = { sdkDirectory.toString() },
      processExecutor = executor,
      executableResolver = resolver,
    ).analyze(sdkDirectory)

    assertTrue(dartResult is DartAnalyzeRunResult.Success)
    assertEquals(listOf(listOf(sdkDirectory.resolve(dartExecutableName).toString(), "analyze", "--format=json", ".")), executor.commands)
  }

  @Test
  fun `resolves Windows batch wrappers before executables`() {
    val sdkDirectory = Files.createDirectory(tempDirectory.resolve("dart-sdk"))
    val resolver = configuredExecutableResolver({ sdkDirectory.toString() }) { Files.isRegularFile(it) }

    assertEquals(DartExecutable.Unusable(sdkDirectory.toString()), resolver.resolve("dart", true))
    val dartExecutable = Files.createFile(sdkDirectory.resolve("dart.exe"))
    assertEquals(DartExecutable.Available(dartExecutable), resolver.resolve("dart", true))
    val dartBatchFile = Files.createFile(sdkDirectory.resolve("dart.bat"))
    assertEquals(DartExecutable.Available(dartBatchFile), resolver.resolve("dart", true))
    assertEquals(listOf("dart.bat", "dart.exe"), executableNames("dart", true))
    assertEquals(listOf("dart"), executableNames("dart", false))
  }

  @Test
  fun `resolves Dart next to a configured Flutter executable`() {
    val sdkDirectory = Files.createDirectory(tempDirectory.resolve("flutter-sdk"))
    Files.createFile(sdkDirectory.resolve("flutter.bat"))
    val dartExecutable = Files.createFile(sdkDirectory.resolve("dart.bat"))

    val result = configuredExecutableResolver({ sdkDirectory.resolve("flutter.bat").toString() }) { Files.isRegularFile(it) }
      .resolve("dart", true)

    assertEquals(DartExecutable.Available(dartExecutable), result)
    assertEquals(listOf("flutter.bat", "flutter.exe"), executableNames("flutter", true))
  }

  private fun noDiagnosticsResourceText(): String =
    requireNotNull(javaClass.getResource("/dart-analyze/no-diagnostics.json")).readText()

  private fun runnerWithoutExecutable(executablePath: () -> String?): DartExecutableRunner = DartExecutableRunner(
    executablePath,
    FakeExecutor(),
    configuredExecutableResolver(executablePath, pathExecutableResolver = { null }),
  )

  private class FakeExecutor(vararg results: DartAnalyzeProcessResult) : DartAnalyzeProcessExecutor {
    private val results = ArrayDeque(results.asList())
    val commands = ArrayList<List<String>>()

    override suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult {
      commands.add(command)
      return results.removeFirst()
    }
  }
}
