package org.jetbrains.qodana.dart

import com.intellij.openapi.util.SystemInfo
import com.intellij.testFramework.LoggedErrorProcessor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

class DartAnalyzeRunnerTest {
  @TempDir
  lateinit var tempDirectory: Path

  @Test
  fun `reports SDK probe startup failures`() = runBlocking {
    val result = DartExecutableRunner(
      processExecutor = FailingExecutor(),
      executableResolver = availableExecutableResolver,
    ).checkSdk(tempDirectory)

    assertTrue(requireNotNull(result).message.contains("Failed to run dart --version: Unable to start Dart"))
  }

  @Test
  fun `rethrows cancellation during the SDK probe`() {
    val executor = CanceledExecutor(cancelOnCall = 1)
    val runner = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver)

    assertThrows(CancellationException::class.java) {
      runBlocking { runner.checkSdk(tempDirectory) }
    }
  }

  @Test
  fun `reports malformed JSON output`() = runBlocking {
    val runner = DartExecutableRunner(
      processExecutor = FakeExecutor(DartAnalyzeProcessResult(0, "not JSON", "")),
      executableResolver = availableExecutableResolver,
    )

    val failure = assertThrows(IllegalStateException::class.java) { runBlocking { runner.analyze(tempDirectory) } }
    assertTrue(failure.message.orEmpty().contains("Failed to parse dart analyze JSON output"))
    assertTrue(failure.message.orEmpty().contains(tempDirectory.toString()))
    assertTrue(failure.cause != null)
  }

  @Test
  fun `keeps diagnostics from nonzero Dart exit and analyzes the module from its directory`() = runBlocking {
    val executor = FakeExecutor(DartAnalyzeProcessResult(2, diagnosticsResourceText(), "failure"))

    val runner = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver)
    val result = runner.analyze(tempDirectory)

    assertEquals(listOf(listOf("dart", "analyze", "--format=json", ".")), executor.commands)
    assertEquals(listOf(tempDirectory), executor.workingDirectories)
    assertEquals(3, result.problems.size)
  }

  @Test
  fun `reports an analyze failure without diagnostics`() = runBlocking {
    val executor = FakeExecutor(DartAnalyzeProcessResult(3, "{\"diagnostics\":[]}", "Analyzer failed"))

    val runner = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver)
    val failure = assertThrows(IllegalStateException::class.java) { runBlocking { runner.analyze(tempDirectory) } }
    assertTrue(failure.message.orEmpty().contains("dart analyze exited with code 3: Analyzer failed"))
    assertEquals(listOf(listOf("dart", "analyze", "--format=json", ".")), executor.commands)
  }

  @Test
  fun `reports process startup failures`() = runBlocking {
    val runner = DartExecutableRunner(
      processExecutor = FailingExecutor(),
      executableResolver = availableExecutableResolver,
    )

    val failure = assertThrows(IllegalStateException::class.java) { runBlocking { runner.analyze(tempDirectory) } }
    assertTrue(failure.message.orEmpty().contains("Failed to run dart analyze: Unable to start Dart"))
    assertTrue(failure.cause is IOException)
  }

  @Test
  fun `rethrows CancellationException during analyze`() {
    val executor = CanceledExecutor(cancelOnCall = 1)
    val runner = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver)

    assertThrows(CancellationException::class.java) {
      runBlocking { runner.analyze(tempDirectory) }
    }

    assertEquals(1, executor.callCount)
  }

  @Test
  fun `reports an unavailable executable`() = runBlocking {
    val runner = DartExecutableRunner({ "/missing/dart" }, FakeExecutor())
    val failure = assertThrows(IllegalStateException::class.java) { runBlocking { runner.analyze(tempDirectory) } }
    assertTrue(failure.message.orEmpty().contains("not executable"))
  }

  @Test
  fun `reports missing executable configuration`() = runBlocking {
    val runner = runnerWithoutExecutable { null }
    val failure = assertThrows(IllegalStateException::class.java) { runBlocking { runner.analyze(tempDirectory) } }
    assertTrue(failure.message.orEmpty().contains("must point to the SDK bin directory"))
  }

  @Test
  fun `reports a blank executable configuration`() = runBlocking {
    val runner = runnerWithoutExecutable { "  " }
    val failure = assertThrows(IllegalStateException::class.java) { runBlocking { runner.analyze(tempDirectory) } }
    assertTrue(failure.message.orEmpty().contains("must point to the SDK bin directory"))
  }

  @Test
  fun `reports an SDK directory without a dart executable`() = runBlocking {
    val sdkDirectory = Files.createDirectory(tempDirectory.resolve("dart-sdk"))

    val runner = DartExecutableRunner({ sdkDirectory.toString() }, FakeExecutor())
    val failure = assertThrows(IllegalStateException::class.java) { runBlocking { runner.analyze(sdkDirectory) } }
    assertTrue(failure.message.orEmpty().contains("not executable"))
  }

  @Test
  fun `logs an invalid executable path and reports it as unusable`() {
    val path = "dart-sdk/invalid\u0000/dart"
    var loggedException: Throwable? = null
    LoggedErrorProcessor.executeWith<Nothing>(object : LoggedErrorProcessor() {
      override fun processWarn(category: String, message: String, t: Throwable?): Boolean {
        loggedException = t
        return false
      }
    }) {
      assertEquals(DartExecutable.Unusable(path), configuredExecutableResolver({ path }).resolve("dart", SystemInfo.isWindows))
    }

    val failure = assertInstanceOf(InvalidPathException::class.java, loggedException)
    assertEquals(path, failure.input)
    assertTrue(failure.stackTrace.isNotEmpty())
  }

  @Test
  fun `resolves Dart in the configured SDK directory`() {
    val sdkDirectory = Files.createDirectory(tempDirectory.resolve("dart-sdk"))
    val dartExecutableName = if (SystemInfo.isWindows) "dart.exe" else "dart"
    val dartExecutable = Files.createFile(sdkDirectory.resolve(dartExecutableName))
    val resolver = configuredExecutableResolver({ sdkDirectory.toString() }) { Files.isRegularFile(it) }

    assertEquals(DartExecutable.Available(dartExecutable), resolver.resolve("dart", SystemInfo.isWindows))
  }

  @Test
  fun `uses the resolved Dart executable for analysis`() = runBlocking {
    val dartExecutable = tempDirectory.resolve("dart-sdk/dart")
    val executor = FakeExecutor(DartAnalyzeProcessResult(0, noDiagnosticsResourceText(), ""))
    val runner = DartExecutableRunner(
      processExecutor = executor,
      executableResolver = DartExecutableResolver { _, _ -> DartExecutable.Available(dartExecutable) },
    )

    runner.analyze(tempDirectory)

    assertEquals(listOf(listOf(dartExecutable.toString(), "analyze", "--format=json", ".")), executor.commands)
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

  private fun diagnosticsResourceText(): String = requireNotNull(javaClass.getResource("/dart-analyze/diagnostics.json")).readText()

  private fun noDiagnosticsResourceText(): String =
    requireNotNull(javaClass.getResource("/dart-analyze/no-diagnostics.json")).readText()

  private fun runnerWithoutExecutable(executablePath: () -> String?): DartExecutableRunner = DartExecutableRunner(
    executablePath,
    FakeExecutor(),
    configuredExecutableResolver(executablePath, pathExecutableResolver = { null }),
  )

  private val availableExecutableResolver = DartExecutableResolver { executableName, _ ->
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

  private class CanceledExecutor(private val cancelOnCall: Int) : DartAnalyzeProcessExecutor {
    var callCount = 0

    override suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult {
      callCount++
      if (callCount >= cancelOnCall) throw CancellationException()
      return DartAnalyzeProcessResult(0, "", "")
    }
  }
}
